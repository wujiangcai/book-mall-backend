package top.wjc.bookmallbackend.controller.front;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.wjc.bookmallbackend.common.Result;
import top.wjc.bookmallbackend.service.CategoryService;
import top.wjc.bookmallbackend.vo.CategoryTreeVO;

import java.util.List;

@RestController
@RequestMapping("/api/front/categories")
public class FrontCategoryController {

    private final CategoryService categoryService;

    public FrontCategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public Result<List<CategoryTreeVO>> list() {
        return Result.success(categoryService.getFrontTree());
    }
}
