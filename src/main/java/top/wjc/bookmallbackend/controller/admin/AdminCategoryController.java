package top.wjc.bookmallbackend.controller.admin;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.wjc.bookmallbackend.common.Result;
import top.wjc.bookmallbackend.dto.CategoryCreateRequest;
import top.wjc.bookmallbackend.dto.CategoryStatusRequest;
import top.wjc.bookmallbackend.dto.CategoryUpdateRequest;
import top.wjc.bookmallbackend.service.CategoryService;
import top.wjc.bookmallbackend.vo.CategoryAdminVO;

import java.util.List;

@RestController
@RequestMapping("/api/admin/categories")
public class AdminCategoryController {

    private final CategoryService categoryService;

    public AdminCategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public Result<List<CategoryAdminVO>> list() {
        return Result.success(categoryService.listAdmin());
    }

    @PostMapping
    public Result<Void> create(@Valid @RequestBody CategoryCreateRequest request) {
        categoryService.create(request);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody CategoryUpdateRequest request) {
        categoryService.update(id, request);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return Result.success();
    }

    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @Valid @RequestBody CategoryStatusRequest request) {
        categoryService.updateStatus(id, request);
        return Result.success();
    }
}
