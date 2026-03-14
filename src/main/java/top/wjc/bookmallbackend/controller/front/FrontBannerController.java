package top.wjc.bookmallbackend.controller.front;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.wjc.bookmallbackend.common.Result;
import top.wjc.bookmallbackend.service.BannerService;
import top.wjc.bookmallbackend.vo.BannerVO;

import java.util.List;

@RestController
@RequestMapping("/api/front/banners")
public class FrontBannerController {

    private final BannerService bannerService;

    public FrontBannerController(BannerService bannerService) {
        this.bannerService = bannerService;
    }

    @GetMapping
    public Result<List<BannerVO>> list() {
        return Result.success(bannerService.listFront());
    }
}
