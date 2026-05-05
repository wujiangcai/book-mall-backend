package top.wjc.bookmallbackend.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
/**
 * 订单商品项对象。
 */
public class OrderItemVO {
    private Long bookId;
    private String bookName;
    private BigDecimal price;
    private Integer quantity;
    private BigDecimal totalPrice;
}
