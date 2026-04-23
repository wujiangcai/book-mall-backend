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
                            ObjectProvider<AlipayClient> alipayClientProvider) {
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
        this.cartMapper = cartMapper;
        this.bookMapper = bookMapper;
        this.addressMapper = addressMapper;
        this.alipayClient = alipayClientProvider.getIfAvailable();
    }

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
        if (!markPaymentSuccess(order, payTime, tradeNo)) {
            return false;
        }
        List<OrderItemVO> items = orderItemMapper.selectByOrderId(order.getId());
        if (items == null || items.isEmpty()) {
            throw new BusinessException(500, "订单商品不存在");
        }
        for (OrderItemVO item : items) {
            decreaseStock(item);
        }
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

    @Override
    public PageResult<AdminOrderListItemVO> listAdmin(Integer page, Integer pageSize, Integer status, String orderNo, Long userId) {
        int currentPage = normalizePage(page);
        int size = normalizeSize(pageSize);
        int offset = (currentPage - 1) * size;
        long total = orderMapper.countAdminList(status, orderNo, userId);
        List<AdminOrderListItemVO> list = orderMapper.selectAdminList(offset, size, status, orderNo, userId);
        return new PageResult<>(total, list, currentPage, size);
    }

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

    private boolean markPaymentSuccess(Order order, LocalDateTime payTime, String tradeNo) {
        Order currentOrder = order;
        for (int attempt = 0; attempt < OPTIMISTIC_LOCK_RETRY_TIMES; attempt++) {
            int updated = orderMapper.updatePaymentSuccess(currentOrder.getId(), OrderStatus.UNPAID.getCode(),
                    OrderStatus.PENDING_SHIP.getCode(), payTime, tradeNo, currentOrder.getVersion());
            if (updated == 1) {
                return true;
            }
            Order latestOrder = orderMapper.selectById(currentOrder.getId());
            if (latestOrder == null) {
                return false;
            }
            if (isPaymentHandled(latestOrder)) {
                return true;
            }
            if (latestOrder.getStatus() == null || latestOrder.getStatus() != OrderStatus.UNPAID.getCode()) {
                return false;
            }
            currentOrder = latestOrder;
        }
        throwOptimisticLockConflict();
        return false;
    }

    private void decreaseStock(OrderItemVO item) {
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
        throw new BusinessException(OPTIMISTIC_LOCK_CODE, OPTIMISTIC_LOCK_MESSAGE);
    }

    private int normalizePage(Integer page) {
        return page == null || page < 1 ? 1 : page;
    }

    private int normalizeSize(Integer pageSize) {
        int size = pageSize == null ? DEFAULT_PAGE_SIZE : pageSize;
        if (size < 1) {
            return DEFAULT_PAGE_SIZE;
        }
        return Math.min(size, MAX_PAGE_SIZE);
    }
}
