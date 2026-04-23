package top.wjc.bookmallbackend.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class AdminOrderListItemVO {
    private Long id;
    private Integer version;
    private String orderNo;
    private Long userId;
    private String username;
    private BigDecimal totalAmount;
    private Integer status;
    private LocalDateTime createTime;
}
