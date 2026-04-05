package top.wjc.bookmallbackend.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardRecentOrderVO {
    private Long id;
    private String orderNo;
    private String username;
    private BigDecimal totalAmount;
    private Integer status;
    private String createTime;
}
