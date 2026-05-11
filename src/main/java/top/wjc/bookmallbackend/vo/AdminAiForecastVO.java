package top.wjc.bookmallbackend.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
/**
 * AI 经营预测结果。
 */
public class AdminAiForecastVO {
    private Long nextWeekOrders;
    private BigDecimal nextWeekRevenue;
    private String confidence;
    private String basis;
}
