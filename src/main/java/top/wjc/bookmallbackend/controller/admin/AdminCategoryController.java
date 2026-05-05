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
import top.wjc.bookmallbackend.dto.CategoryCreateRequest;
import top.wjc.bookmallbackend.dto.CategoryStatusRequest;
import top.wjc.bookmallbackend.dto.CategoryUpdateRequest;
import top.wjc.bookmallbackend.service.CategoryService;
import top.wjc.bookmallbackend.vo.CategoryAdminVO;

/**
 * 后台分类管理控制器。
 *
 * <p>负责分类的增删改查和启停用，前台图书筛选依赖这里维护的数据。
 */
@Validated
@RestController
@RequestMapping("/api/admin/categories")
public class AdminCategoryController {

    private final CategoryService categoryService;

    public AdminCategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    /**
     * 后台分页查询分类。
     */
    public Result<PageResult<CategoryAdminVO>> list(@RequestParam(required = false) @Min(value = 1, message = "page必须大于0") Integer page,
                                                    @RequestParam(required = false) @Min(value = 1, message = "pageSize必须大于0") Integer pageSize) {
        return Result.success(categoryService.listAdmin(page, pageSize));
    }

    @PostMapping
    /**
     * 新增分类。
     */
    public Result<Void> create(@Valid @RequestBody CategoryCreateRequest request) {
        categoryService.create(request);
        return Result.success();
    }

    @PutMapping("/{id}")
    /**
     * 修改分类。
     */
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody CategoryUpdateRequest request) {
        categoryService.update(id, request);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    /**
     * 删除分类。
     */
    public Result<Void> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return Result.success();
    }

    @PutMapping("/{id}/status")
    /**
     * 修改分类状态。
     */
    public Result<Void> updateStatus(@PathVariable Long id, @Valid @RequestBody CategoryStatusRequest request) {
        categoryService.updateStatus(id, request);
        return Result.success();
    }
}
