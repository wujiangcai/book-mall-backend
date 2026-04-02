package top.wjc.bookmallbackend.service.impl;

import com.alipay.api.AlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.test.util.ReflectionTestUtils;
import top.wjc.bookmallbackend.constant.BookStatus;
import top.wjc.bookmallbackend.constant.OrderStatus;
import top.wjc.bookmallbackend.dto.AdminOrderUpdateRequest;
import top.wjc.bookmallbackend.entity.Book;
import top.wjc.bookmallbackend.entity.Order;
import top.wjc.bookmallbackend.exception.InsufficientStockException;
import top.wjc.bookmallbackend.exception.InvalidOrderStatusException;
import top.wjc.bookmallbackend.mapper.AddressMapper;
import top.wjc.bookmallbackend.mapper.BookMapper;
import top.wjc.bookmallbackend.mapper.CartMapper;
import top.wjc.bookmallbackend.mapper.OrderItemMapper;
import top.wjc.bookmallbackend.mapper.OrderMapper;
import top.wjc.bookmallbackend.vo.OrderItemVO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderMapper orderMapper;
    @Mock
    private OrderItemMapper orderItemMapper;
    @Mock
    private CartMapper cartMapper;
    @Mock
    private BookMapper bookMapper;
    @Mock
    private AddressMapper addressMapper;
    @Mock
    private AlipayClient alipayClient;
    @Mock
    private ObjectProvider<AlipayClient> alipayClientProvider;

    private OrderServiceImpl orderService;

    @BeforeEach
    void setUp() {
        when(alipayClientProvider.getIfAvailable()).thenReturn(alipayClient);
        orderService = new OrderServiceImpl(orderMapper, orderItemMapper, cartMapper, bookMapper, addressMapper, alipayClientProvider);
        ReflectionTestUtils.setField(orderService, "alipayAppId", "test-app-id");
        ReflectionTestUtils.setField(orderService, "alipaySellerId", "test-seller-id");
        ReflectionTestUtils.setField(orderService, "alipayPublicKey", "configured-public-key");
        ReflectionTestUtils.setField(orderService, "alipayCharset", "UTF-8");
        ReflectionTestUtils.setField(orderService, "alipaySignType", "RSA2");
    }

    private org.mockito.MockedStatic<AlipaySignature> mockAlipaySignSuccess() {
        org.mockito.MockedStatic<AlipaySignature> mocked = mockStatic(AlipaySignature.class);
        mocked.when(() -> AlipaySignature.rsaCheckV1(any(), eq("configured-public-key"), eq("UTF-8"), eq("RSA2")))
                .thenReturn(true);
        return mocked;
    }

    @Test
    void pay_shouldNotDecreaseStockBeforePaymentSuccess() {
        Long userId = 1L;
        Long orderId = 10L;
        Order order = Order.builder()
                .id(orderId)
                .orderNo("ORDER-1")
                .status(OrderStatus.UNPAID.getCode())
                .totalAmount(new BigDecimal("99.00"))
                .build();
        Book book = Book.builder()
                .id(100L)
                .status(BookStatus.ON_SHELF.getCode())
                .stock(5)
                .build();
        OrderItemVO item = new OrderItemVO(100L, "Book", new BigDecimal("99.00"), 2, new BigDecimal("198.00"));

        when(orderMapper.selectByIdAndUserId(orderId, userId)).thenReturn(order);
        when(orderItemMapper.selectByOrderId(orderId)).thenReturn(List.of(item));
        when(bookMapper.selectById(100L)).thenReturn(book);

        assertThrows(RuntimeException.class, () -> orderService.pay(userId, orderId));
        verify(bookMapper, never()).decreaseStock(any(), any());
    }

    @Test
    void handleAlipayNotify_shouldRejectWhenAppIdMismatch() {
        Order order = Order.builder()
                .id(11L)
                .orderNo("ORDER-11")
                .status(OrderStatus.UNPAID.getCode())
                .totalAmount(new BigDecimal("88.00"))
                .build();

        when(orderMapper.selectByOrderNo("ORDER-11")).thenReturn(order);

        boolean result;
        try (org.mockito.MockedStatic<AlipaySignature> ignored = mockAlipaySignSuccess()) {
            result = orderService.handleAlipayNotify(Map.of(
                    "trade_status", "TRADE_SUCCESS",
                    "out_trade_no", "ORDER-11",
                    "total_amount", "88.00",
                    "app_id", "wrong-app-id",
                    "seller_id", "test-seller-id"
            ));
        }

        assertFalse(result);
        verify(orderMapper, never()).updatePaymentSuccess(any(), any(), any(), any(), any());
        verify(bookMapper, never()).decreaseStock(any(), any());
    }

    @Test
    void handleAlipayNotify_shouldRejectWhenSellerIdMismatch() {
        Order order = Order.builder()
                .id(12L)
                .orderNo("ORDER-12")
                .status(OrderStatus.UNPAID.getCode())
                .totalAmount(new BigDecimal("66.00"))
                .build();

        when(orderMapper.selectByOrderNo("ORDER-12")).thenReturn(order);

        boolean result;
        try (org.mockito.MockedStatic<AlipaySignature> ignored = mockAlipaySignSuccess()) {
            result = orderService.handleAlipayNotify(Map.of(
                    "trade_status", "TRADE_SUCCESS",
                    "out_trade_no", "ORDER-12",
                    "total_amount", "66.00",
                    "app_id", "test-app-id",
                    "seller_id", "wrong-seller-id"
            ));
        }

        assertFalse(result);
        verify(orderMapper, never()).updatePaymentSuccess(any(), any(), any(), any(), any());
        verify(bookMapper, never()).decreaseStock(any(), any());
    }

    @Test
    void handleAlipayNotify_shouldReturnTrueWithoutRepeatingStockDeductionWhenAlreadyHandled() {
        Order order = Order.builder()
                .id(13L)
                .orderNo("ORDER-13")
                .status(OrderStatus.PENDING_SHIP.getCode())
                .payTime(LocalDateTime.now())
                .tradeNo("TRADE-13")
                .totalAmount(new BigDecimal("77.00"))
                .build();

        when(orderMapper.selectByOrderNo("ORDER-13")).thenReturn(order);

        boolean result;
        try (org.mockito.MockedStatic<AlipaySignature> ignored = mockAlipaySignSuccess()) {
            result = orderService.handleAlipayNotify(Map.of(
                    "trade_status", "TRADE_SUCCESS",
                    "out_trade_no", "ORDER-13",
                    "total_amount", "77.00",
                    "app_id", "test-app-id",
                    "seller_id", "test-seller-id"
            ));
        }

        assertTrue(result);
        verify(orderMapper, never()).updatePaymentSuccess(any(), any(), any(), any(), any());
        verify(bookMapper, never()).decreaseStock(any(), any());
    }

    @Test
    void handleAlipayNotify_shouldDeductStockOnceWhenPaymentSucceeds() {
        Order order = Order.builder()
                .id(14L)
                .orderNo("ORDER-14")
                .status(OrderStatus.UNPAID.getCode())
                .totalAmount(new BigDecimal("58.00"))
                .build();
        OrderItemVO item = new OrderItemVO(101L, "Book", new BigDecimal("29.00"), 2, new BigDecimal("58.00"));

        when(orderMapper.selectByOrderNo("ORDER-14")).thenReturn(order);
        when(orderMapper.updatePaymentSuccess(eq(14L), eq(OrderStatus.UNPAID.getCode()), eq(OrderStatus.PENDING_SHIP.getCode()), any(LocalDateTime.class), eq("TRADE-14")))
                .thenReturn(1);
        when(orderItemMapper.selectByOrderId(14L)).thenReturn(List.of(item));
        when(bookMapper.decreaseStock(101L, 2)).thenReturn(1);

        boolean result;
        try (org.mockito.MockedStatic<AlipaySignature> ignored = mockAlipaySignSuccess()) {
            result = orderService.handleAlipayNotify(Map.of(
                    "trade_status", "TRADE_SUCCESS",
                    "out_trade_no", "ORDER-14",
                    "total_amount", "58.00",
                    "app_id", "test-app-id",
                    "seller_id", "test-seller-id",
                    "trade_no", "TRADE-14"
            ));
        }

        assertTrue(result);
        verify(orderMapper).updatePaymentSuccess(eq(14L), eq(OrderStatus.UNPAID.getCode()), eq(OrderStatus.PENDING_SHIP.getCode()), any(LocalDateTime.class), eq("TRADE-14"));
        verify(bookMapper).decreaseStock(101L, 2);
    }

    @Test
    void handleAlipayNotify_shouldThrowWhenStockDeductionFails() {
        Order order = Order.builder()
                .id(15L)
                .orderNo("ORDER-15")
                .status(OrderStatus.UNPAID.getCode())
                .totalAmount(new BigDecimal("30.00"))
                .build();
        OrderItemVO item = new OrderItemVO(102L, "Book", new BigDecimal("30.00"), 1, new BigDecimal("30.00"));

        when(orderMapper.selectByOrderNo("ORDER-15")).thenReturn(order);
        when(orderMapper.updatePaymentSuccess(eq(15L), eq(OrderStatus.UNPAID.getCode()), eq(OrderStatus.PENDING_SHIP.getCode()), any(LocalDateTime.class), eq("TRADE-15")))
                .thenReturn(1);
        when(orderItemMapper.selectByOrderId(15L)).thenReturn(List.of(item));
        when(bookMapper.decreaseStock(102L, 1)).thenReturn(0);

        try (org.mockito.MockedStatic<AlipaySignature> ignored = mockAlipaySignSuccess()) {
            assertThrows(InsufficientStockException.class, () -> orderService.handleAlipayNotify(Map.of(
                    "trade_status", "TRADE_SUCCESS",
                    "out_trade_no", "ORDER-15",
                    "total_amount", "30.00",
                    "app_id", "test-app-id",
                    "seller_id", "test-seller-id",
                    "trade_no", "TRADE-15"
            )));
        }
    }

    @Test
    void updateAdmin_shouldRejectIllegalStatusJump() {
        Order order = Order.builder()
                .id(16L)
                .userId(1L)
                .status(OrderStatus.UNPAID.getCode())
                .addressId(99L)
                .build();
        AdminOrderUpdateRequest request = new AdminOrderUpdateRequest();
        request.setStatus(OrderStatus.SHIPPED.getCode());

        when(orderMapper.selectById(16L)).thenReturn(order);

        assertThrows(InvalidOrderStatusException.class, () -> orderService.updateAdmin(16L, request));
        verify(orderMapper, never()).updateStatus(any(), any());
    }

    @Test
    void updateAdmin_shouldAllowPendingShipToShipped() {
        Order order = Order.builder()
                .id(17L)
                .userId(1L)
                .status(OrderStatus.PENDING_SHIP.getCode())
                .addressId(99L)
                .build();
        AdminOrderUpdateRequest request = new AdminOrderUpdateRequest();
        request.setStatus(OrderStatus.SHIPPED.getCode());

        when(orderMapper.selectById(17L)).thenReturn(order);

        assertDoesNotThrow(() -> orderService.updateAdmin(17L, request));
        verify(orderMapper).updateStatus(17L, OrderStatus.SHIPPED.getCode());
    }

    @Test
    void cancelAdmin_shouldRejectCompletedOrder() {
        Order order = Order.builder()
                .id(18L)
                .status(OrderStatus.COMPLETED.getCode())
                .build();

        when(orderMapper.selectById(18L)).thenReturn(order);

        assertThrows(InvalidOrderStatusException.class, () -> orderService.cancelAdmin(18L));
        verify(orderMapper, never()).updateStatus(any(), any());
    }
}
