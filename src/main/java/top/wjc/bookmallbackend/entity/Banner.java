package top.wjc.bookmallbackend.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
/**
 * 轮播图实体。
 *
 * <p>对应数据库中的 `banner` 表，主要保存首页轮播图的图片、链接、排序和状态。
 */
public class Banner {
    private Long id;
    private String imageUrl;
    private String linkUrl;
    private Integer sortOrder;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
