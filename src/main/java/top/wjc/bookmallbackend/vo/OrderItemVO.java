package top.wjc.bookmallbackend.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class OrderItemVO {
    private Long bookId;
    private String bookName;
    private BigDecimal price;
    private Integer quantity;
    private BigDecimal totalPrice;
}
