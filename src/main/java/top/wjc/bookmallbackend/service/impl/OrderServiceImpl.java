package top.wjc.bookmallbackend.service.impl;

import com.alipay.api.AlipayClient;
import com.alipay.api.AlipayApiException;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.response.AlipayTradePagePayResponse;
import com.alipay.api.internal.util.AlipaySignature;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.wjc.bookmallbackend.common.PageResult;
import top.wjc.bookmallbackend.constant.BookStatus;
import top.wjc.bookmallbackend.constant.OrderStatus;
import top.wjc.bookmallbackend.dto.AdminOrderCreateRequest;
import top.wjc.bookmallbackend.dto.AdminOrderUpdateRequest;
import top.wjc.bookmallbackend.dto.OrderCreateRequest;
import top.wjc.bookmallbackend.entity.Address;
import top.wjc.bookmallbackend.entity.Book;
import top.wjc.bookmallbackend.entity.Cart;
import top.wjc.bookmallbackend.entity.Order;
import top.wjc.bookmallbackend.entity.OrderItem;
import top.wjc.bookmallbackend.exception.BookOffShelfException;
import top.wjc.bookmallbackend.exception.BusinessException;
import top.wjc.bookmallbackend.exception.InsufficientStockException;
import top.wjc.bookmallbackend.exception.InvalidOrderStatusException;
import top.wjc.bookmallbackend.exception.NotFoundException;
import top.wjc.bookmallbackend.mapper.AddressMapper;
import top.wjc.bookmallbackend.mapper.BookMapper;
import top.wjc.bookmallbackend.mapper.CartMapper;
import top.wjc.bookmallbackend.mapper.OrderItemMapper;
import top.wjc.bookmallbackend.mapper.OrderMapper;
import top.wjc.bookmallbackend.service.OrderService;
import top.wjc.bookmallbackend.service.RecommendationService;
import top.wjc.bookmallbackend.vo.AddressSnapshotVO;
import top.wjc.bookmallbackend.vo.AdminOrderDetailVO;
import top.wjc.bookmallbackend.vo.AdminOrderListItemVO;
import top.wjc.bookmallbackend.vo.OrderCreateVO;
import top.wjc.bookmallbackend.vo.OrderDetailVO;
import top.wjc.bookmallbackend.vo.OrderItemVO;
import top.wjc.bookmallbackend.vo.OrderListItemVO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
/**
 * 订单服务实现。
 *
 * <p>这是整个项目业务最复杂的服务，串起了：
 * <p>购物车 -> 创建订单 -> 发起支付 -> 支付回调 -> 扣减库存 -> 后台发货 -> 订单查询。
 *
 * <p>项目在订单和图书表中都使用了 version 字段进行乐观锁控制，
 * 用来降低并发下的超卖、重复支付回调、状态覆盖等问题。
 */
public class OrderServiceImpl implements OrderService {

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;
    private static final int OPTIMISTIC_LOCK_CODE = 409;
    private static final String OPTIMISTIC_LOCK_MESSAGE = "数据已变更，请刷新后重试";
    private static final int OPTIMISTIC_LOCK_RETRY_TIMES = 3;

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final CartMapper cartMapper;
    private final BookMapper bookMapper;
    private final AddressMapper addressMapper;
    private final AlipayClient alipayClient;
    private final RecommendationService recommendationService;

    private static final String ALIPAY_TRADE_SUCCESS = "TRADE_SUCCESS";
    private static final String ALIPAY_TRADE_FINISHED = "TRADE_FINISHED";

    @Value("${alipay.notify-url}")
    private String alipayNotifyUrl;

    @Value("${alipay.return-url}")
    private String alipayReturnUrl;

    @Value("${alipay.app-id:}")
    private String alipayAppId;

    @Value("${alipay.seller-id:}")
    private String alipaySellerId;

    @Value("${alipay.public-key:}")
    private String alipayPublicKey;

    @Value("${alipay.charset}")
    private String alipayCharset;

    @Value("${alipay.sign-type}")
    private String alipaySignType;

    public OrderServiceImpl(OrderMapper orderMapper,
                            OrderItemMapper orderItemMapper,
                            CartMapper cartMapper,
                            BookMapper bookMapper,
                            AddressMapper addressMapper,
                            ObjectProvider<AlipayClient> alipayClientProvider,
                            RecommendationService recommendationService) {
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
        this.cartMapper = cartMapper;
        this.bookMapper = bookMapper;
        this.addressMapper = addressMapper;
        this.alipayClient = alipayClientProvider.getIfAvailable();
        this.recommendationService = recommendationService;
    }

    /**
     * 前台创建订单。
     *
     * <p>主要步骤：
     * <p>1. 校验地址属于当前用户；
     * <p>2. 根据 cartIds 读取购物车项；
     * <p>3. 生成订单主表与订单明细；
     * <p>4. 计算订单总金额；
     * <p>5. 删除已下单的购物车项。
     *
     * <p>注意：项目选择在“支付成功回调”时再真正扣库存，而不是在创建订单时扣库存。
     */
    @Override
    @Transactional
    public OrderCreateVO create(Long userId, OrderCreateRequest request) {
        Address address = addressMapper.selectById(request.getAddressId());
        if (address == null || !address.getUserId().equals(userId)) {
            throw new NotFoundException();
        }
        List<Cart> carts = cartMapper.selectByIdsAndUserId(request.getCartIds(), userId);
        if (carts == null || carts.isEmpty()) {
            throw new NotFoundException();
        }
        if (carts.size() != request.getCartIds().size()) {
            throw new NotFoundException();
        }

        List<OrderItem> items = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (Cart cart : carts) {
            Book book = bookMapper.selectById(cart.getBookId());
            if (book == null || isSoftDeleted(book.getStatus())) {
                throw new NotFoundException();
            }
            if (book.getStatus() == null || book.getStatus() != BookStatus.ON_SHELF.getCode()) {
                throw new BookOffShelfException();
            }
            if (book.getStock() == null || cart.getQuantity() > book.getStock()) {
                throw new InsufficientStockException();
            }
            BigDecimal itemTotal = book.getPrice().multiply(BigDecimal.valueOf(cart.getQuantity()));
            totalAmount = totalAmount.add(itemTotal);
            items.add(OrderItem.builder()
                    .bookId(book.getId())
                    .bookName(book.getBookName())
                    .price(book.getPrice())
                    .quantity(cart.getQuantity())
                    .totalPrice(itemTotal)
                    .build());
        }

        LocalDateTime now = LocalDateTime.now();
        Order order = Order.builder()
                .orderNo(generateOrderNo(userId))
                .userId(userId)
                .totalAmount(totalAmount)
                .status(OrderStatus.UNPAID.getCode())
                .addressId(address.getId())
                .build();
        orderMapper.insert(order);

        for (OrderItem item : items) {
            item.setOrderId(order.getId());
        }
        orderItemMapper.batchInsert(items);
        cartMapper.deleteByIdsAndUserId(request.getCartIds(), userId);

        AddressSnapshotVO snapshot = buildAddressSnapshot(address);
        List<OrderItemVO> itemVOs = items.stream()
                .map(item -> new OrderItemVO(item.getBookId(), item.getBookName(), item.getPrice(), item.getQuantity(), item.getTotalPrice()))
                .collect(Collectors.toList());
        return new OrderCreateVO(order.getId(), order.getOrderNo(), order.getTotalAmount(), order.getStatus(),
                order.getCreateTime() == null ? now : order.getCreateTime(), snapshot, itemVOs);
    }

    /**
     * 发起支付宝支付。
     *
     * <p>这里会生成一个 HTML form，前端拿到后直接渲染/跳转到支付宝页面。
     */
    @Override
    @Transactional
    public String pay(Long userId, Long orderId) {
        Order order = orderMapper.selectByIdAndUserId(orderId, userId);
        if (order == null) {
            throw new NotFoundException();
        }
        if (order.getStatus() == null || order.getStatus() != OrderStatus.UNPAID.getCode()) {
            throw new InvalidOrderStatusException();
        }
        if (order.getPayTime() != null || order.getTradeNo() != null) {
            throw new InvalidOrderStatusException();
        }
        List<OrderItemVO> items = orderItemMapper.selectByOrderId(orderId);
        if (items == null || items.isEmpty()) {
            throw new NotFoundException();
        }
        for (OrderItemVO item : items) {
            Book book = bookMapper.selectById(item.getBookId());
            if (book == null || isSoftDeleted(book.getStatus())) {
                throw new NotFoundException();
            }
            if (book.getStatus() == null || book.getStatus() != BookStatus.ON_SHELF.getCode()) {
                throw new BookOffShelfException();
            }
            if (book.getStock() == null || item.getQuantity() > book.getStock()) {
                throw new InsufficientStockException();
            }
        }

        return buildAlipayPage(order);
    }

    /**
     * 处理支付宝异步通知。
     *
     * <p>这是支付链路里最关键的一步。只有异步通知校验通过后，系统才会：
     * 更新订单状态为“待发货”并扣减库存。
     */
    @Override
    @Transactional
    public boolean handleAlipayNotify(Map<String, String> params) {
        if (!verifyAlipaySign(params)) {
            return false;
        }
        String outTradeNo = params.get("out_trade_no");
        if (outTradeNo == null || outTradeNo.isBlank()) {
            return false;
        }
        Order order = orderMapper.selectByOrderNo(outTradeNo);
        if (order == null) {
            return false;
        }
        return processAlipayPayment(order, params, true);
    }

    /**
     * 处理支付宝同步跳转。
     *
     * <p>用户支付完成后浏览器会回到系统前端页面。这里主要用于辅助确认支付结果，
     * 但真正可靠的入账依据仍然是异步通知。
     */
    @Override
    @Transactional
    public Long handleAlipayReturn(Map<String, String> params) {
        if (!verifyAlipaySign(params)) {
            return null;
        }
        String outTradeNo = params.get("out_trade_no");
        if (outTradeNo == null || outTradeNo.isBlank()) {
            return null;
        }
        Order order = orderMapper.selectByOrderNo(outTradeNo);
        if (order == null) {
            return null;
        }
        if (!processAlipayPayment(order, params, false)) {
            return null;
        }
        return order.getId();
    }

    private String buildAlipayPage(Order order) {
        // 把订单信息组装成支付宝要求的支付表单 HTML，由前端直接提交。
        if (alipayClient == null) {
            throw new BusinessException(500, "支付宝未配置");
        }
        try {
            AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
            request.setNotifyUrl(alipayNotifyUrl);
            request.setReturnUrl(alipayReturnUrl);
            String subject = "图书订单" + order.getOrderNo();
            String bizContent = "{" +
                    "\"out_trade_no\":\"" + order.getOrderNo() + "\"," +
                    "\"product_code\":\"FAST_INSTANT_TRADE_PAY\"," +
                    "\"total_amount\":\"" + order.getTotalAmount() + "\"," +
                    "\"subject\":\"" + subject + "\"" +
                    "}";
            request.setBizContent(bizContent);
            AlipayTradePagePayResponse response = alipayClient.pageExecute(request);
            if (!response.isSuccess()) {
                throw new BusinessException(500, "支付宝支付发起失败");
            }
            return response.getBody();
        } catch (AlipayApiException ex) {
            throw new BusinessException(500, "支付宝支付发起失败");
        }
    }

    private boolean verifyAlipaySign(Map<String, String> params) {
        if (alipayPublicKey == null || alipayPublicKey.isBlank()) {
            return false;
        }
        try {
            return AlipaySignature.rsaCheckV1(params, alipayPublicKey, alipayCharset, alipaySignType);
        } catch (AlipayApiException ex) {
            return false;
        }
    }

    private boolean isValidAlipayApp(Map<String, String> params) {
        if (alipayAppId == null || alipayAppId.isBlank()) {
            return false;
        }
        return Objects.equals(alipayAppId, params.get("app_id"));
    }

    private boolean isValidAlipaySeller(Map<String, String> params) {
        if (alipaySellerId == null || alipaySellerId.isBlank()) {
            return true;
        }
        String sellerId = params.get("seller_id");
        return sellerId != null && !sellerId.isBlank() && Objects.equals(alipaySellerId, sellerId);
    }

    private boolean isValidAlipayAmount(Order order, String totalAmountParam) {
        BigDecimal totalAmount = order.getTotalAmount();
        if (totalAmount == null || totalAmountParam == null || totalAmountParam.isBlank()) {
            return false;
        }
        try {
            return totalAmount.compareTo(new BigDecimal(totalAmountParam)) == 0;
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    private boolean isPaymentHandled(Order order) {
        return order.getStatus() != null
                && order.getStatus() == OrderStatus.PENDING_SHIP.getCode()
                && order.getPayTime() != null;
    }

    private boolean processAlipayPayment(Order order, Map<String, String> params, boolean requireTradeSuccess) {
        // 统一封装同步回跳和异步通知的支付处理逻辑，避免两套代码分叉。
        if (order == null) {
            return false;
        }
        if (isPaymentHandled(order)) {
            return true;
        }
        if (requireTradeSuccess) {
            String tradeStatus = params.get("trade_status");
            if (!ALIPAY_TRADE_SUCCESS.equals(tradeStatus) && !ALIPAY_TRADE_FINISHED.equals(tradeStatus)) {
                return false;
            }
        }
        if (!isValidAlipayApp(params) || !isValidAlipaySeller(params) || !isValidAlipayAmount(order, params.get("total_amount"))) {
            return false;
        }
        if (order.getStatus() == null || order.getStatus() != OrderStatus.UNPAID.getCode()) {
            return false;
        }
        LocalDateTime payTime = LocalDateTime.now();
        String tradeNo = normalizeTradeNo(params.get("trade_no"));
        PaymentMarkResult paymentMarkResult = markPaymentSuccess(order, payTime, tradeNo);
        if (paymentMarkResult == PaymentMarkResult.FAILED) {
            return false;
        }
        if (paymentMarkResult == PaymentMarkResult.ALREADY_HANDLED) {
            return true;
        }
        List<OrderItemVO> items = orderItemMapper.selectByOrderId(order.getId());
        if (items == null || items.isEmpty()) {
            throw new BusinessException(500, "订单商品不存在");
        }
        for (OrderItemVO item : items) {
            decreaseStock(item);
        }
        recommendationService.recordPurchase(order.getUserId(), toOrderItems(items, order.getId()));
        return true;
    }

    private String normalizeTradeNo(String tradeNo) {
        if (tradeNo == null || tradeNo.isBlank()) {
            return null;
        }
        return tradeNo;
    }

    private boolean canUpdateAddress(Integer status) {
        return status != null
                && status != OrderStatus.SHIPPED.getCode()
                && status != OrderStatus.COMPLETED.getCode()
                && status != OrderStatus.CANCELLED.getCode();
    }

    private boolean canAdminUpdateStatus(Integer currentStatus, Integer targetStatus) {
        if (currentStatus == null || targetStatus == null) {
            return false;
        }
        if (Objects.equals(currentStatus, targetStatus)) {
            return true;
        }
        if (targetStatus == OrderStatus.CANCELLED.getCode()) {
            return canCancel(currentStatus);
        }
        return currentStatus == OrderStatus.PENDING_SHIP.getCode() && targetStatus == OrderStatus.SHIPPED.getCode();
    }

    private boolean canCancel(Integer status) {
        return status != null
                && status != OrderStatus.SHIPPED.getCode()
                && status != OrderStatus.COMPLETED.getCode()
                && status != OrderStatus.CANCELLED.getCode();
    }

    /**
     * 前台取消订单。
     */
    @Override
    @Transactional
    public void cancel(Long userId, Long orderId) {
        Order order = orderMapper.selectByIdAndUserId(orderId, userId);
        if (order == null) {
            throw new NotFoundException();
        }
        if (order.getStatus() == null || order.getStatus() != OrderStatus.UNPAID.getCode()) {
            throw new InvalidOrderStatusException();
        }
        if (orderMapper.updateStatus(orderId, OrderStatus.CANCELLED.getCode(), order.getVersion()) == 0) {
            throwOptimisticLockConflict();
        }
    }

    /**
     * 前台确认收货。
     *
     * <p>只有订单归属当前用户，且状态为“已发货”时，才允许用户确认收货并流转为“已完成”。
     */
    @Override
    @Transactional
    public void confirmReceipt(Long userId, Long orderId) {
        Order order = orderMapper.selectByIdAndUserId(orderId, userId);
        if (order == null) {
            throw new NotFoundException();
        }
        if (order.getStatus() == null || order.getStatus() != OrderStatus.SHIPPED.getCode()) {
            throw new InvalidOrderStatusException();
        }
        if (orderMapper.updateStatus(orderId, OrderStatus.COMPLETED.getCode(), order.getVersion()) == 0) {
            throwOptimisticLockConflict();
        }
    }

    /**
     * 前台订单分页查询。
     */
    @Override
    public PageResult<OrderListItemVO> list(Long userId, Integer page, Integer pageSize) {
        int currentPage = normalizePage(page);
        int size = normalizeSize(pageSize);
        int offset = (currentPage - 1) * size;
        long total = orderMapper.countByUserId(userId);
        List<Order> orders = orderMapper.selectByUserId(userId, offset, size);
        List<OrderListItemVO> list = orders.stream()
                .map(order -> new OrderListItemVO(order.getId(), order.getOrderNo(), order.getTotalAmount(),
                        order.getStatus(), order.getCreateTime(), buildItemSummary(order.getId())))
                .collect(Collectors.toList());
        return new PageResult<>(total, list, currentPage, size);
    }

    /**
     * 前台订单详情查询。
     */
    @Override
    public OrderDetailVO detail(Long userId, Long orderId) {
        Order order = orderMapper.selectByIdAndUserId(orderId, userId);
        if (order == null) {
            throw new NotFoundException();
        }
        List<OrderItemVO> items = orderItemMapper.selectByOrderId(orderId);
        Address address = addressMapper.selectById(order.getAddressId());
        if (address == null) {
            throw new NotFoundException();
        }
        AddressSnapshotVO snapshot = buildAddressSnapshot(address);
        return new OrderDetailVO(order.getId(), order.getOrderNo(), order.getTotalAmount(), order.getStatus(),
                order.getCreateTime(), order.getPayTime(), snapshot, items);
    }

    /**
     * 后台订单分页查询。
     */
    @Override
    public PageResult<AdminOrderListItemVO> listAdmin(Integer page, Integer pageSize, Integer status, String orderNo, Long userId) {
        int currentPage = normalizePage(page);
        int size = normalizeSize(pageSize);
        int offset = (currentPage - 1) * size;
        long total = orderMapper.countAdminList(status, orderNo, userId);
        List<AdminOrderListItemVO> list = orderMapper.selectAdminList(offset, size, status, orderNo, userId);
        return new PageResult<>(total, list, currentPage, size);
    }

    /**
     * 后台代客创建订单。
     */
    @Override
    @Transactional
    public void createAdmin(AdminOrderCreateRequest request) {
        Address address = addressMapper.selectById(request.getAddressId());
        if (address == null || !address.getUserId().equals(request.getUserId())) {
            throw new NotFoundException();
        }
        List<AdminOrderCreateRequest.AdminOrderItemRequest> itemsRequest = request.getItems();
        if (itemsRequest == null || itemsRequest.isEmpty()) {
            throw new NotFoundException();
        }
        List<OrderItem> items = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (AdminOrderCreateRequest.AdminOrderItemRequest itemRequest : itemsRequest) {
            Book book = bookMapper.selectById(itemRequest.getBookId());
            if (book == null || isSoftDeleted(book.getStatus())) {
                throw new NotFoundException();
            }
            if (book.getStatus() == null || book.getStatus() != BookStatus.ON_SHELF.getCode()) {
                throw new BookOffShelfException();
            }
            if (book.getStock() == null || itemRequest.getQuantity() == null || itemRequest.getQuantity() < 1
                    || itemRequest.getQuantity() > book.getStock()) {
                throw new InsufficientStockException();
            }
            BigDecimal itemTotal = book.getPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity()));
            totalAmount = totalAmount.add(itemTotal);
            items.add(OrderItem.builder()
                    .bookId(book.getId())
                    .bookName(book.getBookName())
                    .price(book.getPrice())
                    .quantity(itemRequest.getQuantity())
                    .totalPrice(itemTotal)
                    .build());
        }
        Order order = Order.builder()
                .orderNo(generateOrderNo(request.getUserId()))
                .userId(request.getUserId())
                .totalAmount(totalAmount)
                .status(OrderStatus.UNPAID.getCode())
                .addressId(address.getId())
                .build();
        orderMapper.insert(order);
        for (OrderItem item : items) {
            item.setOrderId(order.getId());
        }
        orderItemMapper.batchInsert(items);
    }

    /**
     * 后台修改订单。
     *
     * <p>管理员不能随意跨状态跳转，必须符合既定状态机规则。
     */
    @Override
    @Transactional
    public void updateAdmin(Long orderId, AdminOrderUpdateRequest request) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new NotFoundException();
        }
        if (request.getAddressId() == null && request.getStatus() == null) {
            throw new BusinessException(400, "至少修改一项订单信息");
        }
        int expectedVersion = resolveVersion(request.getVersion(), order.getVersion());
        if (request.getAddressId() != null && !request.getAddressId().equals(order.getAddressId())) {
            Address address = addressMapper.selectById(request.getAddressId());
            if (address == null || !address.getUserId().equals(order.getUserId())) {
                throw new NotFoundException();
            }
            if (!canUpdateAddress(order.getStatus())) {
                throw new InvalidOrderStatusException();
            }
            if (orderMapper.updateAddress(orderId, request.getAddressId(), expectedVersion) == 0) {
                throwOptimisticLockConflict();
            }
            expectedVersion++;
            order.setVersion(expectedVersion);
            order.setAddressId(request.getAddressId());
        }
        if (request.getStatus() != null && !request.getStatus().equals(order.getStatus())) {
            if (!canAdminUpdateStatus(order.getStatus(), request.getStatus())) {
                throw new InvalidOrderStatusException();
            }
            if (orderMapper.updateStatus(orderId, request.getStatus(), expectedVersion) == 0) {
                throwOptimisticLockConflict();
            }
            expectedVersion++;
            order.setVersion(expectedVersion);
            order.setStatus(request.getStatus());
        }
    }

    /**
     * 后台取消订单。
     */
    @Override
    @Transactional
    public void cancelAdmin(Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new NotFoundException();
        }
        if (!canCancel(order.getStatus())) {
            throw new InvalidOrderStatusException();
        }
        if (orderMapper.updateStatus(orderId, OrderStatus.CANCELLED.getCode(), order.getVersion()) == 0) {
            throwOptimisticLockConflict();
        }
    }

    /**
     * 后台订单详情查询。
     */
    @Override
    public AdminOrderDetailVO detailAdmin(Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new NotFoundException();
        }
        List<OrderItemVO> items = orderItemMapper.selectByOrderId(orderId);
        Address address = addressMapper.selectById(order.getAddressId());
        if (address == null) {
            throw new NotFoundException();
        }
        AddressSnapshotVO snapshot = buildAddressSnapshot(address);
        return new AdminOrderDetailVO(order.getId(), order.getVersion(), order.getOrderNo(), order.getUserId(),
                order.getTotalAmount(), order.getStatus(), order.getAddressId(), order.getCreateTime(),
                order.getPayTime(), order.getShipTime(), snapshot, items);
    }

    /**
     * 后台发货。
     *
     * <p>只有“待发货”状态允许变为“已发货”。
     */
    @Override
    @Transactional
    public void ship(Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new NotFoundException();
        }
        if (order.getStatus() == null || order.getStatus() != OrderStatus.PENDING_SHIP.getCode()) {
            throw new InvalidOrderStatusException();
        }
        if (orderMapper.updateShipment(orderId, OrderStatus.PENDING_SHIP.getCode(),
                OrderStatus.SHIPPED.getCode(), LocalDateTime.now(), order.getVersion()) == 0) {
            throwOptimisticLockConflict();
        }
    }

    private PaymentMarkResult markPaymentSuccess(Order order, LocalDateTime payTime, String tradeNo) {
        // 支付回调可能因为网络重试而重复触发，这里用乐观锁 + 重试保证幂等性。
        Order currentOrder = order;
        for (int attempt = 0; attempt < OPTIMISTIC_LOCK_RETRY_TIMES; attempt++) {
            int updated = orderMapper.updatePaymentSuccess(currentOrder.getId(), OrderStatus.UNPAID.getCode(),
                    OrderStatus.PENDING_SHIP.getCode(), payTime, tradeNo, currentOrder.getVersion());
            if (updated == 1) {
                return PaymentMarkResult.UPDATED;
            }
            Order latestOrder = orderMapper.selectById(currentOrder.getId());
            if (latestOrder == null) {
                return PaymentMarkResult.FAILED;
            }
            if (isPaymentHandled(latestOrder)) {
                return PaymentMarkResult.ALREADY_HANDLED;
            }
            if (latestOrder.getStatus() == null || latestOrder.getStatus() != OrderStatus.UNPAID.getCode()) {
                return PaymentMarkResult.FAILED;
            }
            currentOrder = latestOrder;
        }
        throwOptimisticLockConflict();
        return PaymentMarkResult.FAILED;
    }

    private void decreaseStock(OrderItemVO item) {
        // 库存扣减同样通过 version 保证并发安全，避免多用户同时购买导致超卖。
        for (int attempt = 0; attempt < OPTIMISTIC_LOCK_RETRY_TIMES; attempt++) {
            Book book = bookMapper.selectById(item.getBookId());
            if (book == null || isSoftDeleted(book.getStatus())) {
                throw new NotFoundException();
            }
            if (book.getStatus() == null || book.getStatus() != BookStatus.ON_SHELF.getCode()) {
                throw new BookOffShelfException();
            }
            if (book.getStock() == null || item.getQuantity() > book.getStock()) {
                throw new InsufficientStockException();
            }
            if (bookMapper.decreaseStock(item.getBookId(), item.getQuantity(), book.getVersion()) == 1) {
                return;
            }
        }
        throwOptimisticLockConflict();
    }

    private List<OrderItem> toOrderItems(List<OrderItemVO> items, Long orderId) {
        return items.stream()
                .map(item -> OrderItem.builder()
                        .orderId(orderId)
                        .bookId(item.getBookId())
                        .bookName(item.getBookName())
                        .price(item.getPrice())
                        .quantity(item.getQuantity())
                        .totalPrice(item.getTotalPrice())
                        .build())
                .collect(Collectors.toList());
    }

    private String buildItemSummary(Long orderId) {
        List<OrderItemVO> items = orderItemMapper.selectByOrderId(orderId);
        if (items == null || items.isEmpty()) {
            return "";
        }
        OrderItemVO first = items.get(0);
        int totalCount = items.stream().mapToInt(OrderItemVO::getQuantity).sum();
        if (items.size() == 1) {
            return first.getBookName() + " x" + totalCount;
        }
        return first.getBookName() + " 等" + totalCount + "件";
    }

    private AddressSnapshotVO buildAddressSnapshot(Address address) {
        String fullAddress = String.join("", safe(address.getProvince()), safe(address.getCity()),
                safe(address.getDistrict()), safe(address.getDetailAddress()));
        return new AddressSnapshotVO(address.getReceiverName(), address.getPhone(), fullAddress);
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private String generateOrderNo(Long userId) {
        long timestamp = System.currentTimeMillis();
        int random = ThreadLocalRandom.current().nextInt(1000, 10000);
        return timestamp + String.valueOf(userId) + random;
    }

    private boolean isSoftDeleted(Integer status) {
        return status != null && status == -1;
    }

    private int resolveVersion(Integer requestVersion, Integer currentVersion) {
        return requestVersion != null ? requestVersion : currentVersion;
    }

    private void throwOptimisticLockConflict() {
        // 前端看到这类错误时，通常应该提示用户刷新页面后重试。
        throw new BusinessException(OPTIMISTIC_LOCK_CODE, OPTIMISTIC_LOCK_MESSAGE);
    }

    private int normalizePage(Integer page) {
        return page == null || page < 1 ? 1 : page;
    }

    private enum PaymentMarkResult {
        UPDATED,
        ALREADY_HANDLED,
        FAILED
    }

    private int normalizeSize(Integer pageSize) {
        int size = pageSize == null ? DEFAULT_PAGE_SIZE : pageSize;
        if (size < 1) {
            return DEFAULT_PAGE_SIZE;
        }
        return Math.min(size, MAX_PAGE_SIZE);
    }
}
