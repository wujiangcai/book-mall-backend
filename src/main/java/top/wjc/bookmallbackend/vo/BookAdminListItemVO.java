package top.wjc.bookmallbackend.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class BookAdminListItemVO {
    private Long id;
    private Integer version;
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
    private LocalDateTime createTime;
}
