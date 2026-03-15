package top.wjc.bookmallbackend.service.impl;

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
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final CartMapper cartMapper;
    private final BookMapper bookMapper;
    private final AddressMapper addressMapper;

    public OrderServiceImpl(OrderMapper orderMapper,
                            OrderItemMapper orderItemMapper,
                            CartMapper cartMapper,
                            BookMapper bookMapper,
                            AddressMapper addressMapper) {
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
        this.cartMapper = cartMapper;
        this.bookMapper = bookMapper;
        this.addressMapper = addressMapper;
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
    public void pay(Long userId, Long orderId) {
        Order order = orderMapper.selectByIdAndUserId(orderId, userId);
        if (order == null) {
            throw new NotFoundException();
        }
        if (order.getStatus() == null || order.getStatus() != OrderStatus.UNPAID.getCode()) {
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
            if (bookMapper.decreaseStock(book.getId(), item.getQuantity()) == 0) {
                throw new InsufficientStockException();
            }
        }
        orderMapper.updateStatus(orderId, OrderStatus.PENDING_SHIP.getCode());
        orderMapper.updatePayTime(orderId, LocalDateTime.now());
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
        orderMapper.updateStatus(orderId, OrderStatus.CANCELLED.getCode());
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
        List<Order> orders = orderMapper.selectAdminList(offset, size, status, orderNo, userId);
        List<AdminOrderListItemVO> list = orders.stream()
                .map(order -> new AdminOrderListItemVO(order.getId(), order.getOrderNo(), order.getUserId(),
                        order.getTotalAmount(), order.getStatus(), order.getCreateTime()))
                .collect(Collectors.toList());
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
        if (request.getAddressId() != null && !request.getAddressId().equals(order.getAddressId())) {
            Address address = addressMapper.selectById(request.getAddressId());
            if (address == null || !address.getUserId().equals(order.getUserId())) {
                throw new NotFoundException();
            }
            orderMapper.updateAddress(orderId, request.getAddressId());
        }
        if (request.getStatus() != null && !request.getStatus().equals(order.getStatus())) {
            orderMapper.updateStatus(orderId, request.getStatus());
        }
    }

    @Override
    @Transactional
    public void cancelAdmin(Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new NotFoundException();
        }
        orderMapper.updateStatus(orderId, OrderStatus.CANCELLED.getCode());
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
        return new AdminOrderDetailVO(order.getId(), order.getOrderNo(), order.getUserId(), order.getTotalAmount(),
                order.getStatus(), order.getAddressId(), order.getCreateTime(), order.getPayTime(), order.getShipTime(), snapshot, items);
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
        orderMapper.updateStatus(orderId, OrderStatus.SHIPPED.getCode());
        orderMapper.updateShipTime(orderId, LocalDateTime.now());
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
