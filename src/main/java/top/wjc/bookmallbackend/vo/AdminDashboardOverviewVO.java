package top.wjc.bookmallbackend.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardOverviewVO {
    private Long customerCount;
    private Long adminCount;
    private Long totalBooks;
    private Long onShelfBooks;
    private Long totalOrders;
    private Long pendingShipOrders;
    private Long lowStockBooks;
    private Long activeBanners;
    private Long todayOrders;
    private BigDecimal totalRevenue;
    private BigDecimal todayRevenue;
}
