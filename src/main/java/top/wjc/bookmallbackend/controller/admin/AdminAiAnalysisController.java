package top.wjc.bookmallbackend.controller.admin;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.wjc.bookmallbackend.common.Result;
import top.wjc.bookmallbackend.service.AdminAiAnalysisService;
import top.wjc.bookmallbackend.vo.AdminAiAnalysisVO;

@RestController
@RequestMapping("/api/admin/ai-analysis")
/**
 * 后台 AI 店铺数据分析控制器。
 */
public class AdminAiAnalysisController {

    private final AdminAiAnalysisService adminAiAnalysisService;

    public AdminAiAnalysisController(AdminAiAnalysisService adminAiAnalysisService) {
        this.adminAiAnalysisService = adminAiAnalysisService;
    }

    @GetMapping
    public Result<AdminAiAnalysisVO> analyze() {
        return Result.success(adminAiAnalysisService.analyze());
    }
}
