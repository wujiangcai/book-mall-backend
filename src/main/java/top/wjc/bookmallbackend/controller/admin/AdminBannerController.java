package top.wjc.bookmallbackend.controller.admin;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.wjc.bookmallbackend.common.PageResult;
import top.wjc.bookmallbackend.common.Result;
import top.wjc.bookmallbackend.dto.BannerCreateRequest;
import top.wjc.bookmallbackend.dto.BannerSortRequest;
import top.wjc.bookmallbackend.dto.BannerUpdateRequest;
import top.wjc.bookmallbackend.service.BannerService;
import top.wjc.bookmallbackend.vo.BannerVO;

/**
 * 后台轮播图管理控制器。
 *
 * <p>管理员可以在这里维护首页轮播图的图片、跳转链接、排序与状态。
 */
@Validated
@RestController
@RequestMapping("/api/admin/banners")
public class AdminBannerController {

    private final BannerService bannerService;

    public AdminBannerController(BannerService bannerService) {
        this.bannerService = bannerService;
    }

    @GetMapping
    /**
     * 后台分页查询轮播图。
     */
    public Result<PageResult<BannerVO>> list(@RequestParam(required = false) @Min(value = 1, message = "page必须大于0") Integer page,
                                             @RequestParam(required = false) @Min(value = 1, message = "pageSize必须大于0") Integer pageSize) {
        return Result.success(bannerService.listAdmin(page, pageSize));
    }

    @PostMapping
    /**
     * 新增轮播图。
     */
    public Result<Void> create(@Valid @RequestBody BannerCreateRequest request) {
        bannerService.create(request);
        return Result.success();
    }

    @PutMapping("/{id}")
    /**
     * 修改轮播图。
     */
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody BannerUpdateRequest request) {
        bannerService.update(id, request);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    /**
     * 删除轮播图。
     */
    public Result<Void> delete(@PathVariable Long id) {
        bannerService.delete(id);
        return Result.success();
    }

    @PutMapping("/{id}/sort")
    /**
     * 调整轮播图排序值。
     */
    public Result<Void> updateSort(@PathVariable Long id, @Valid @RequestBody BannerSortRequest request) {
        bannerService.updateSort(id, request);
        return Result.success();
    }
}
