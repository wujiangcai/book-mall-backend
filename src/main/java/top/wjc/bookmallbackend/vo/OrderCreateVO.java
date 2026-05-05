package top.wjc.bookmallbackend.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
/**
 * 创建订单成功后的返回对象。
 *
 * <p>前端通常拿它来展示下单成功页或直接进入支付流程。
 */
public class OrderCreateVO {
    private Long orderId;
    private String orderNo;
    private BigDecimal totalAmount;
    private Integer status;
    private LocalDateTime createTime;
    private AddressSnapshotVO address;
    private List<OrderItemVO> items;
}
