package top.wjc.bookmallbackend.controller.admin;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.wjc.bookmallbackend.common.Result;
import top.wjc.bookmallbackend.service.DashboardService;
import top.wjc.bookmallbackend.vo.AdminDashboardVO;

/**
 * 后台数据看板控制器。
 *
 * <p>聚合返回首页总览、趋势、排行、低库存等统计数据。
 */
@RestController
@RequestMapping("/api/admin/dashboard")
public class AdminDashboardController {

    private final DashboardService dashboardService;

    public AdminDashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    /**
     * 查询后台首页看板数据。
     */
    public Result<AdminDashboardVO> getDashboard() {
        return Result.success(dashboardService.getAdminDashboard());
    }
}
