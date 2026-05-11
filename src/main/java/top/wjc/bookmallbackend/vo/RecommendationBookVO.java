package top.wjc.bookmallbackend.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
/**
 * 混合推荐图书项。
 *
 * <p>除图书基础信息外，还返回推荐分数、命中的推荐策略和推荐理由，便于前台解释推荐结果。
 */
public class RecommendationBookVO {
    private Long id;
    private String bookName;
    private String author;
    private String publisher;
    private Long categoryId;
    private Integer stock;
    private BigDecimal price;
    private String coverImage;
    private String categoryName;
    private Double score;
    private String strategy;
    private String reason;
}