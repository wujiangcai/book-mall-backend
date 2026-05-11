package top.wjc.bookmallbackend.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
/**
 * AI 经营洞察项。
 */
public class AdminAiInsightVO {
    private String type;
    private String level;
    private String title;
    private String content;
    private String action;
}
