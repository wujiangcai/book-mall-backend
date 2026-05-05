package top.wjc.bookmallbackend.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
/**
 * 后台首页看板总响应对象。
 *
 * <p>把总览、趋势、状态分布、热门图书、低库存图书等统计数据一次性聚合返回。
 */
public class AdminDashboardVO {
    private AdminDashboardOverviewVO overview;
    private List<AdminDashboardTrendPointVO> orderTrend;
    private List<AdminDashboardOrderStatusVO> orderStatus;
    private List<AdminDashboardCategoryStatVO> categoryStats;
    private List<AdminDashboardHotBookVO> hotBooks;
    private List<AdminDashboardLowStockBookVO> lowStockBooks;
    private List<AdminDashboardRecentOrderVO> recentOrders;
}
