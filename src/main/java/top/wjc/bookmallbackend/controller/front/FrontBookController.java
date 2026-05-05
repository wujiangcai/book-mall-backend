package top.wjc.bookmallbackend.controller.front;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.wjc.bookmallbackend.common.PageResult;
import top.wjc.bookmallbackend.common.Result;
import top.wjc.bookmallbackend.service.BookService;
import top.wjc.bookmallbackend.vo.BookDetailVO;
import top.wjc.bookmallbackend.vo.BookListItemVO;

import java.math.BigDecimal;

/**
 * 前台图书控制器。
 *
 * <p>提供公开的图书列表与详情接口，未登录用户也可以访问。
 */
@Validated
@RestController
@RequestMapping("/api/front/books")
public class FrontBookController {

    private final BookService bookService;

    public FrontBookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping
    /**
     * 图书分页查询，支持分类、关键词和价格区间筛选。
     */
    public Result<PageResult<BookListItemVO>> list(@RequestParam(required = false) @Min(value = 1, message = "page必须大于0") Integer page,
                                                   @RequestParam(required = false) @Min(value = 1, message = "pageSize必须大于0") Integer pageSize,
                                                   @RequestParam(required = false) @Min(value = 1, message = "categoryId必须大于0") Long categoryId,
                                                   @RequestParam(required = false) String keyword,
                                                   @RequestParam(required = false) @DecimalMin(value = "0", message = "minPrice必须大于等于0") BigDecimal minPrice,
                                                   @RequestParam(required = false) @DecimalMin(value = "0", message = "maxPrice必须大于等于0") BigDecimal maxPrice) {
        return Result.success(bookService.listFront(page, pageSize, categoryId, keyword, minPrice, maxPrice));
    }

    @GetMapping("/{id}")
    /**
     * 图书详情查询。
     */
    public Result<BookDetailVO> detail(@PathVariable Long id) {
        return Result.success(bookService.getFrontDetail(id));
    }
}
