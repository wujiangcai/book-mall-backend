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
 * 购物车实体。
 *
 * <p>对应数据库中的 `cart` 表，用来暂存用户待购买的图书和数量。
 */
public class Cart {
    private Long id;
    private Long userId;
    private Long bookId;
    private Integer quantity;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
