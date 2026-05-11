package top.wjc.bookmallbackend.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
/**
 * AI 经营分析关键指标。
 */
public class AdminAiMetricVO {
    private String label;
    private String value;
    private String trend;
    private String hint;
}
