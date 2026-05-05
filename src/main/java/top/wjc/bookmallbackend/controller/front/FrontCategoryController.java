package top.wjc.bookmallbackend.controller.front;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.wjc.bookmallbackend.common.Result;
import top.wjc.bookmallbackend.service.CategoryService;
import top.wjc.bookmallbackend.vo.CategoryTreeVO;

import java.util.List;

/**
 * 前台分类控制器。
 *
 * <p>返回前台可用的分类树结构，方便页面直接渲染导航或筛选栏。
 */
@RestController
@RequestMapping("/api/front/categories")
public class FrontCategoryController {

    private final CategoryService categoryService;

    public FrontCategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    /**
     * 查询前台分类树。
     */
    public Result<List<CategoryTreeVO>> list() {
        return Result.success(categoryService.getFrontTree());
    }
}
