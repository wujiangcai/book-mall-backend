package top.wjc.bookmallbackend.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import top.wjc.bookmallbackend.vo.AdminDashboardCategoryStatVO;
import top.wjc.bookmallbackend.vo.AdminDashboardHotBookVO;
import top.wjc.bookmallbackend.vo.AdminDashboardLowStockBookVO;
import top.wjc.bookmallbackend.vo.AdminDashboardOrderStatusVO;
import top.wjc.bookmallbackend.vo.AdminDashboardOverviewVO;
import top.wjc.bookmallbackend.vo.AdminDashboardRecentOrderVO;
import top.wjc.bookmallbackend.vo.AdminDashboardTrendPointVO;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface DashboardMapper {
    AdminDashboardOverviewVO selectOverview(@Param("todayStart") LocalDateTime todayStart,
                                            @Param("tomorrowStart") LocalDateTime tomorrowStart,
                                            @Param("lowStockThreshold") Integer lowStockThreshold);

    List<AdminDashboardTrendPointVO> selectOrderTrend(@Param("startTime") LocalDateTime startTime);

    List<AdminDashboardOrderStatusVO> selectOrderStatusStats();

    List<AdminDashboardCategoryStatVO> selectCategoryBookStats(@Param("limit") int limit);

    List<AdminDashboardHotBookVO> selectHotBooks(@Param("limit") int limit);

    List<AdminDashboardLowStockBookVO> selectLowStockBooks(@Param("lowStockThreshold") Integer lowStockThreshold,
                                                           @Param("limit") int limit);

    List<AdminDashboardRecentOrderVO> selectRecentOrders(@Param("limit") int limit);
}
