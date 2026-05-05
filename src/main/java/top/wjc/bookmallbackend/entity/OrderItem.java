package top.wjc.bookmallbackend.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
/**
 * 订单明细实体。
 *
 * <p>对应数据库中的 `order_item` 表，一张订单通常对应多条订单明细记录。
 */
public class OrderItem {
    private Long id;
    private Long orderId;
    private Long bookId;
    private String bookName;
    private BigDecimal price;
    private Integer quantity;
    private BigDecimal totalPrice;
}
