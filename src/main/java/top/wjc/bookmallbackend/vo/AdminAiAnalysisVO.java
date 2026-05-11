package top.wjc.bookmallbackend.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
/**
 * 后台 AI 店铺数据分析响应对象。
 */
public class AdminAiAnalysisVO {
    private String summary;
    private List<AdminAiMetricVO> metrics;
    private AdminAiForecastVO forecast;
    private List<AdminAiInsightVO> insights;
    private List<AdminDashboardHotBookVO> focusBooks;
    private String source;
    private String sourceLabel;
    private LocalDateTime generatedAt;
}