package top.wjc.bookmallbackend.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardVO {
    private AdminDashboardOverviewVO overview;
    private List<AdminDashboardTrendPointVO> orderTrend;
    private List<AdminDashboardOrderStatusVO> orderStatus;
    private List<AdminDashboardCategoryStatVO> categoryStats;
    private List<AdminDashboardHotBookVO> hotBooks;
    private List<AdminDashboardLowStockBookVO> lowStockBooks;
    private List<AdminDashboardRecentOrderVO> recentOrders;
}
