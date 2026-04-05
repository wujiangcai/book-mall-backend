package top.wjc.bookmallbackend.service.impl;

import org.springframework.stereotype.Service;
import top.wjc.bookmallbackend.mapper.DashboardMapper;
import top.wjc.bookmallbackend.service.DashboardService;
import top.wjc.bookmallbackend.service.UploadService;
import top.wjc.bookmallbackend.vo.AdminDashboardCategoryStatVO;
import top.wjc.bookmallbackend.vo.AdminDashboardHotBookVO;
import top.wjc.bookmallbackend.vo.AdminDashboardTrendPointVO;
import top.wjc.bookmallbackend.vo.AdminDashboardVO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class DashboardServiceImpl implements DashboardService {

    private static final int LOW_STOCK_THRESHOLD = 10;
    private static final int TOP_CATEGORY_LIMIT = 6;
    private static final int HOT_BOOK_LIMIT = 5;
    private static final int LOW_STOCK_LIMIT = 6;
    private static final int RECENT_ORDER_LIMIT = 6;
    private static final DateTimeFormatter DAY_FORMATTER = DateTimeFormatter.ofPattern("MM-dd");

    private final DashboardMapper dashboardMapper;
    private final UploadService uploadService;

    public DashboardServiceImpl(DashboardMapper dashboardMapper, UploadService uploadService) {
        this.dashboardMapper = dashboardMapper;
        this.uploadService = uploadService;
    }

    @Override
    public AdminDashboardVO getAdminDashboard() {
        LocalDate today = LocalDate.now();
        LocalDateTime todayStart = today.atStartOfDay();
        LocalDateTime tomorrowStart = today.plusDays(1).atStartOfDay();
        LocalDateTime trendStart = today.minusDays(6).atStartOfDay();

        List<AdminDashboardTrendPointVO> trend = fillMissingTrendDays(dashboardMapper.selectOrderTrend(trendStart), today);
        List<AdminDashboardCategoryStatVO> categoryStats = dashboardMapper.selectCategoryBookStats(TOP_CATEGORY_LIMIT);
        List<AdminDashboardHotBookVO> hotBooks = dashboardMapper.selectHotBooks(HOT_BOOK_LIMIT).stream()
                .map(book -> new AdminDashboardHotBookVO(
                        book.getBookId(),
                        book.getBookName(),
                        book.getAuthor(),
                        uploadService.resolveBookCoverUrl(book.getCoverImage()),
                        book.getSalesCount(),
                        book.getSalesAmount()
                ))
                .collect(Collectors.toList());

        return new AdminDashboardVO(
                dashboardMapper.selectOverview(todayStart, tomorrowStart, LOW_STOCK_THRESHOLD),
                trend,
                dashboardMapper.selectOrderStatusStats(),
                categoryStats,
                hotBooks,
                dashboardMapper.selectLowStockBooks(LOW_STOCK_THRESHOLD, LOW_STOCK_LIMIT),
                dashboardMapper.selectRecentOrders(RECENT_ORDER_LIMIT)
        );
    }

    private List<AdminDashboardTrendPointVO> fillMissingTrendDays(List<AdminDashboardTrendPointVO> rawTrend, LocalDate endDay) {
        Map<String, AdminDashboardTrendPointVO> trendMap = rawTrend.stream()
                .collect(Collectors.toMap(AdminDashboardTrendPointVO::getDay, Function.identity(), (left, right) -> left));
        List<AdminDashboardTrendPointVO> filled = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            String day = endDay.minusDays(i).format(DAY_FORMATTER);
            AdminDashboardTrendPointVO point = trendMap.get(day);
            if (point == null) {
                filled.add(new AdminDashboardTrendPointVO(day, 0L, BigDecimal.ZERO));
            } else {
                filled.add(point);
            }
        }
        return filled;
    }
}
