package top.wjc.bookmallbackend.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class CartItemVO {
    private Long id;
    private Long bookId;
    private String bookName;
    private BigDecimal price;
    private Integer quantity;
    private Integer stock;
    private String coverImage;
    private BigDecimal totalPrice;
}
