package top.wjc.bookmallbackend.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
/**
 * 前台图书详情对象。
 */
public class BookDetailVO {
    private Long id;
    private String bookName;
    private String author;
    private String publisher;
    private String isbn;
    private BigDecimal price;
    private Integer stock;
    private String coverImage;
    private String description;
    private Long categoryId;
    private String categoryName;
    private Integer status;
}
