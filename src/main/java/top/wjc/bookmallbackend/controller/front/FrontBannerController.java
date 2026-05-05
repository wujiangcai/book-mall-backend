package top.wjc.bookmallbackend.controller.front;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.wjc.bookmallbackend.common.Result;
import top.wjc.bookmallbackend.service.BannerService;
import top.wjc.bookmallbackend.vo.BannerVO;

import java.util.List;

/**
 * 前台轮播图控制器。
 *
 * <p>首页进入时通常会先加载轮播图，因此这里提供公开查询接口。
 */
@RestController
@RequestMapping("/api/front/banners")
public class FrontBannerController {

    private final BannerService bannerService;

    public FrontBannerController(BannerService bannerService) {
        this.bannerService = bannerService;
    }

    @GetMapping
    /**
     * 查询前台轮播图列表。
     */
    public Result<List<BannerVO>> list() {
        return Result.success(bannerService.listFront());
    }
}
