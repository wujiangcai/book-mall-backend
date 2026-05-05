package top.wjc.bookmallbackend.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
/**
 * 图书实体。
 *
 * <p>对应数据库中的 `book` 表，保存图书基础信息、库存、价格以及乐观锁版本号。
 */
public class Book {
    private Long id;
    private Integer version;
    private String bookName;
    private String author;
    private String publisher;
    private String isbn;
    private Long categoryId;
    private BigDecimal price;
    private Integer stock;
    private String coverImage;
    private String description;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
