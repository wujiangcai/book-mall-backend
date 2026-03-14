package top.wjc.bookmallbackend.controller.front;

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

@Validated
@RestController
@RequestMapping("/api/front/books")
public class FrontBookController {

    private final BookService bookService;

    public FrontBookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping
    public Result<PageResult<BookListItemVO>> list(@RequestParam(required = false) @Min(value = 1, message = "page必须大于0") Integer page,
                                                   @RequestParam(required = false) @Min(value = 1, message = "pageSize必须大于0") Integer pageSize,
                                                   @RequestParam(required = false) @Min(value = 1, message = "categoryId必须大于0") Long categoryId,
                                                   @RequestParam(required = false) String keyword) {
        return Result.success(bookService.listFront(page, pageSize, categoryId, keyword));
    }

    @GetMapping("/{id}")
    public Result<BookDetailVO> detail(@PathVariable Long id) {
        return Result.success(bookService.getFrontDetail(id));
    }
}
