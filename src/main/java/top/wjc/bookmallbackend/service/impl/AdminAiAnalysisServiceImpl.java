package top.wjc.bookmallbackend.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.wjc.bookmallbackend.client.OpenAiCompatibleClient;
import top.wjc.bookmallbackend.client.OpenAiCompatibleClient.AiChatResult;
import top.wjc.bookmallbackend.service.AdminAiAnalysisService;
import top.wjc.bookmallbackend.service.DashboardService;
import top.wjc.bookmallbackend.vo.AdminAiAnalysisVO;
import top.wjc.bookmallbackend.vo.AdminAiForecastVO;
import top.wjc.bookmallbackend.vo.AdminAiInsightVO;
import top.wjc.bookmallbackend.vo.AdminAiMetricVO;
import top.wjc.bookmallbackend.vo.AdminDashboardHotBookVO;
import top.wjc.bookmallbackend.vo.AdminDashboardOverviewVO;
import top.wjc.bookmallbackend.vo.AdminDashboardTrendPointVO;
import top.wjc.bookmallbackend.vo.AdminDashboardVO;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
/**
 * AI 店铺数据分析服务实现。
 *
 * <p>优先调用 OpenAI 兼容大模型生成结构化经营分析，支持 ChatGPT、通义千问等模型；
 * 当模型未配置、超时、异常或返回结构不完整时，自动回退到本地“数据聚合 + 规则推理 + 模板生成”。
 */
public class AdminAiAnalysisServiceImpl implements AdminAiAnalysisService {

    private static final int MAX_METRIC_COUNT = 4;
    private static final int MAX_INSIGHT_COUNT = 5;

    private final DashboardService dashboardService;
    private final OpenAiCompatibleClient aiClient;
    private final ObjectMapper objectMapper;

    public AdminAiAnalysisServiceImpl(DashboardService dashboardService,
                                      OpenAiCompatibleClient aiClient,
                                      ObjectMapper objectMapper) {
        this.dashboardService = dashboardService;
        this.aiClient = aiClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public AdminAiAnalysisVO analyze() {
        AdminDashboardVO dashboard = dashboardService.getAdminDashboard();
        AdminAiAnalysisVO fallback = buildRuleBasedAnalysis(dashboard);
        return buildModelAnalysis(dashboard, fallback).orElse(fallback);
    }

    private Optional<AdminAiAnalysisVO> buildModelAnalysis(AdminDashboardVO dashboard, AdminAiAnalysisVO fallback) {
        try {
            String prompt = buildUserPrompt(dashboard, fallback);
            AiChatResult result = aiClient.chat(buildSystemPrompt(), prompt);
            if (result.isTimeout()) {
                markFallback(fallback, "timeout", "AI超时兜底");
                return Optional.empty();
            }
            if (!result.isSuccess()) {
                return Optional.empty();
            }
            return parseModelAnalysis(result.getContent(), fallback);
        } catch (Exception exception) {
            log.warn("AI analysis generation failed, fallback to rule based result, message={}", exception.getMessage());
            return Optional.empty();
        }
    }


    private void markFallback(AdminAiAnalysisVO fallback, String source, String sourceLabel) {
        fallback.setSource(source);
        fallback.setSourceLabel(sourceLabel);
        fallback.setGeneratedAt(LocalDateTime.now());
    }
    private Optional<AdminAiAnalysisVO> parseModelAnalysis(String content, AdminAiAnalysisVO fallback) {
        try {
            JsonNode root = objectMapper.readTree(content);
            String summary = text(root.path("summary"));
            List<AdminAiMetricVO> metrics = parseMetrics(root.path("metrics"));
            AdminAiForecastVO forecast = parseForecast(root.path("forecast"));
            List<AdminAiInsightVO> insights = parseInsights(root.path("insights"));

            if (isBlank(summary) || metrics.isEmpty() || forecast == null || insights.isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(new AdminAiAnalysisVO(summary, metrics, forecast, insights, fallback.getFocusBooks(), "model", "AI模型生成", LocalDateTime.now()));
        } catch (Exception exception) {
            log.warn("AI analysis JSON parse failed, fallback to rule based result", exception);
            return Optional.empty();
        }
    }

    private List<AdminAiMetricVO> parseMetrics(JsonNode metricsNode) {
        List<AdminAiMetricVO> metrics = new ArrayList<>();
        if (!metricsNode.isArray()) {
            return metrics;
        }
        for (JsonNode node : metricsNode) {
            String label = text(node.path("label"));
            String value = text(node.path("value"));
            String trend = text(node.path("trend"));
            String hint = text(node.path("hint"));
            if (!isBlank(label) && !isBlank(value)) {
                metrics.add(new AdminAiMetricVO(label, value, defaultText(trend, "AI分析"), defaultText(hint, "由大模型结合经营数据生成")));
            }
            if (metrics.size() >= MAX_METRIC_COUNT) {
                break;
            }
        }
        return metrics;
    }

    private AdminAiForecastVO parseForecast(JsonNode forecastNode) {
        if (!forecastNode.isObject()) {
            return null;
        }
        Long nextWeekOrders = longValue(forecastNode.path("nextWeekOrders"));
        BigDecimal nextWeekRevenue = decimalValue(forecastNode.path("nextWeekRevenue"));
        String confidence = text(forecastNode.path("confidence"));
        String basis = text(forecastNode.path("basis"));
        if (nextWeekOrders == null || nextWeekRevenue == null) {
            return null;
        }
        return new AdminAiForecastVO(
                Math.max(0, nextWeekOrders),
                nextWeekRevenue.max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP),
                defaultText(confidence, "中"),
                defaultText(basis, "由大模型根据近7天经营趋势和当前库存状态预测")
        );
    }

    private List<AdminAiInsightVO> parseInsights(JsonNode insightsNode) {
        List<AdminAiInsightVO> insights = new ArrayList<>();
        if (!insightsNode.isArray()) {
            return insights;
        }
        for (JsonNode node : insightsNode) {
            String type = text(node.path("type"));
            String level = normalizeLevel(text(node.path("level")));
            String title = text(node.path("title"));
            String itemContent = text(node.path("content"));
            String action = text(node.path("action"));
            if (!isBlank(title) && !isBlank(itemContent) && !isBlank(action)) {
                insights.add(new AdminAiInsightVO(defaultText(type, "operation"), level, title, itemContent, action));
            }
            if (insights.size() >= MAX_INSIGHT_COUNT) {
                break;
            }
        }
        return insights;
    }

    private String buildSystemPrompt() {
        return "你是图书商城后台经营分析专家。必须只输出合法 JSON，不要 Markdown，不要代码块。"
                + "你的输出要稳定匹配前端模板字段，语气专业、简洁、可执行。"
                + "只能基于用户提供的数据分析，不要编造不存在的图书、订单或金额。";
    }

    private String buildUserPrompt(AdminDashboardVO dashboard, AdminAiAnalysisVO fallback) throws Exception {
        return "请基于以下图书商城经营数据生成 AI 智能分析。输出必须是 JSON 对象，字段固定为："
                + "summary:string；metrics:数组4项，每项含label,value,trend,hint；"
                + "forecast:对象，含nextWeekOrders:number,nextWeekRevenue:number,confidence:string,basis:string；"
                + "insights:数组3到5项，每项含type,level,title,content,action，其中level只能是success、warning、info。"
                + "要求：summary 60字以内；每条 insight 不超过45字；action 不超过35字；金额保留两位小数；预测必须保守。"
                + "\n\n经营数据 JSON：\n" + objectMapper.writeValueAsString(dashboard);
    }

    private AdminAiAnalysisVO buildRuleBasedAnalysis(AdminDashboardVO dashboard) {
        AdminDashboardOverviewVO overview = dashboard.getOverview();
        List<AdminDashboardTrendPointVO> trend = dashboard.getOrderTrend() == null ? List.of() : dashboard.getOrderTrend();

        long weekOrders = sumOrders(trend);
        BigDecimal weekRevenue = sumRevenue(trend);
        double avgOrderValue = weekOrders == 0 ? 0 : weekRevenue.divide(BigDecimal.valueOf(weekOrders), 2, RoundingMode.HALF_UP).doubleValue();
        double growthRate = calculateGrowthRate(trend);
        AdminAiForecastVO forecast = buildForecast(weekOrders, weekRevenue, growthRate);
        List<AdminAiInsightVO> insights = buildInsights(dashboard, growthRate, avgOrderValue);
        List<AdminAiMetricVO> metrics = buildMetrics(overview, weekOrders, weekRevenue, growthRate, avgOrderValue);
        String summary = buildSummary(overview, weekOrders, weekRevenue, growthRate, insights.size());
        List<AdminDashboardHotBookVO> focusBooks = dashboard.getHotBooks() == null ? List.of() : dashboard.getHotBooks();

        return new AdminAiAnalysisVO(summary, metrics, forecast, insights, focusBooks, "rule", "规则兜底", LocalDateTime.now());
    }

    private List<AdminAiMetricVO> buildMetrics(AdminDashboardOverviewVO overview,
                                               long weekOrders,
                                               BigDecimal weekRevenue,
                                               double growthRate,
                                               double avgOrderValue) {
        List<AdminAiMetricVO> metrics = new ArrayList<>();
        metrics.add(new AdminAiMetricVO("近7天订单", String.valueOf(weekOrders), formatPercent(growthRate), growthRate >= 0 ? "订单趋势向好" : "订单趋势走低"));
        metrics.add(new AdminAiMetricVO("近7天营收", formatMoney(weekRevenue), "实时", "已支付/待发货/已发货/已完成订单计入营收"));
        metrics.add(new AdminAiMetricVO("客单价", "¥" + formatDecimal(avgOrderValue), "估算", "近7天营收 ÷ 近7天订单"));
        metrics.add(new AdminAiMetricVO("库存预警", String.valueOf(nullSafe(overview.getLowStockBooks())), "需关注", "库存低于阈值的上架图书数量"));
        return metrics;
    }

    private AdminAiForecastVO buildForecast(long weekOrders, BigDecimal weekRevenue, double growthRate) {
        double factor = clamp(1 + growthRate, 0.75, 1.35);
        long nextOrders = Math.max(0, Math.round(weekOrders * factor));
        BigDecimal nextRevenue = weekRevenue.multiply(BigDecimal.valueOf(factor)).setScale(2, RoundingMode.HALF_UP);
        String confidence = weekOrders >= 10 ? "高" : weekOrders >= 3 ? "中" : "低";
        String basis = "基于近7天订单、营收与前后半周增长率进行线性外推";
        return new AdminAiForecastVO(nextOrders, nextRevenue, confidence, basis);
    }

    private List<AdminAiInsightVO> buildInsights(AdminDashboardVO dashboard, double growthRate, double avgOrderValue) {
        List<AdminAiInsightVO> insights = new ArrayList<>();
        AdminDashboardOverviewVO overview = dashboard.getOverview();

        if (growthRate < -0.1) {
            insights.add(new AdminAiInsightVO("trend", "warning", "订单增长放缓",
                    "近7天后半周订单低于前半周，说明流量或转化可能下滑。",
                    "建议在首页突出热销图书，并对低转化分类做限时优惠。"));
        } else if (growthRate > 0.1) {
            insights.add(new AdminAiInsightVO("trend", "success", "订单趋势上升",
                    "近期订单增长明显，可继续放大有效品类和畅销图书曝光。",
                    "建议给热销榜增加首页坑位，并提前补充高销量图书库存。"));
        } else {
            insights.add(new AdminAiInsightVO("trend", "info", "经营走势平稳",
                    "近7天订单波动较小，适合通过推荐算法提升长尾图书曝光。",
                    "建议观察个性化推荐点击率，并逐步调整推荐权重。"));
        }

        if (nullSafe(overview.getLowStockBooks()) > 0) {
            insights.add(new AdminAiInsightVO("stock", "warning", "存在库存风险",
                    "有上架图书已进入低库存区间，若继续销售可能影响履约。",
                    "建议优先处理低库存列表，并结合热销榜设置补货优先级。"));
        }

        if (nullSafe(overview.getPendingShipOrders()) > 0) {
            insights.add(new AdminAiInsightVO("order", "warning", "待发货订单待处理",
                    "当前存在待发货订单，长时间未处理会影响用户体验。",
                    "建议每日固定批量发货，并在订单列表中优先筛选待发货状态。"));
        }

        if (dashboard.getHotBooks() != null && !dashboard.getHotBooks().isEmpty()) {
            AdminDashboardHotBookVO hotBook = dashboard.getHotBooks().get(0);
            insights.add(new AdminAiInsightVO("book", "success", "爆款图书可重点运营",
                    "《" + hotBook.getBookName() + "》当前销量领先，具备带动同类图书销售的潜力。",
                    "建议在详情页使用“看了又看”推荐同分类或同作者图书。"));
        }

        if (avgOrderValue < 50 && avgOrderValue > 0) {
            insights.add(new AdminAiInsightVO("revenue", "info", "客单价偏低",
                    "近7天客单价较低，用户可能以单本购买为主。",
                    "建议增加满减、套装书和同类书联合推荐，提高连带购买。"));
        }

        return insights;
    }

    private String buildSummary(AdminDashboardOverviewVO overview,
                                long weekOrders,
                                BigDecimal weekRevenue,
                                double growthRate,
                                int insightCount) {
        return "AI 已分析当前店铺 " + nullSafe(overview.getTotalBooks()) + " 本图书、"
                + nullSafe(overview.getTotalOrders()) + " 个订单。近7天产生 " + weekOrders
                + " 个订单，营收 " + formatMoney(weekRevenue) + "，趋势" + (growthRate >= 0 ? "上升" : "下降")
                + " " + formatPercent(Math.abs(growthRate)) + "，已生成 " + insightCount + " 条运营建议。";
    }

    private long sumOrders(List<AdminDashboardTrendPointVO> trend) {
        return trend.stream()
                .map(AdminDashboardTrendPointVO::getOrderCount)
                .mapToLong(this::nullSafe)
                .sum();
    }

    private BigDecimal sumRevenue(List<AdminDashboardTrendPointVO> trend) {
        return trend.stream()
                .map(AdminDashboardTrendPointVO::getRevenue)
                .reduce(BigDecimal.ZERO, (left, right) -> left.add(right == null ? BigDecimal.ZERO : right));
    }

    private double calculateGrowthRate(List<AdminDashboardTrendPointVO> trend) {
        if (trend.size() < 4) {
            return 0;
        }
        long firstHalf = trend.subList(0, trend.size() / 2).stream()
                .map(AdminDashboardTrendPointVO::getOrderCount)
                .mapToLong(this::nullSafe)
                .sum();
        long secondHalf = trend.subList(trend.size() / 2, trend.size()).stream()
                .map(AdminDashboardTrendPointVO::getOrderCount)
                .mapToLong(this::nullSafe)
                .sum();
        if (firstHalf == 0) {
            return secondHalf > 0 ? 1 : 0;
        }
        return (secondHalf - firstHalf) / (double) firstHalf;
    }

    private String text(JsonNode node) {
        return node == null || node.isMissingNode() || node.isNull() ? "" : node.asText("").trim();
    }

    private Long longValue(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return null;
        }
        if (node.isNumber()) {
            return node.asLong();
        }
        try {
            return Long.parseLong(node.asText().replaceAll("[^0-9-]", ""));
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private BigDecimal decimalValue(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return null;
        }
        if (node.isNumber()) {
            return node.decimalValue();
        }
        String value = node.asText().replace("¥", "").replace(",", "").trim();
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private String normalizeLevel(String level) {
        return switch (level) {
            case "success", "warning", "info" -> level;
            default -> "info";
        };
    }

    private String defaultText(String value, String fallback) {
        return isBlank(value) ? fallback : value;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private long nullSafe(Long value) {
        return value == null ? 0 : value;
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private String formatPercent(double rate) {
        return formatDecimal(rate * 100) + "%";
    }

    private String formatMoney(BigDecimal value) {
        return "¥" + (value == null ? "0.00" : value.setScale(2, RoundingMode.HALF_UP).toPlainString());
    }

    private String formatDecimal(double value) {
        return BigDecimal.valueOf(value).setScale(1, RoundingMode.HALF_UP).toPlainString();
    }
}
