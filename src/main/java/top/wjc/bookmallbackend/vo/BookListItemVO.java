package top.wjc.bookmallbackend.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class BookListItemVO {
    private Long id;
    private String bookName;
    private String author;
    private String publisher;
    private BigDecimal price;
    private String coverImage;
    private String categoryName;
}
