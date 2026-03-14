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
public class Cart {
    private Long id;
    private Long userId;
    private Long bookId;
    private Integer quantity;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
