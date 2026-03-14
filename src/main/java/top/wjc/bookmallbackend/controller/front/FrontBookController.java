package top.wjc.bookmallbackend.controller.front;

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

@RestController
@RequestMapping("/api/front/books")
public class FrontBookController {

    private final BookService bookService;

    public FrontBookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping
    public Result<PageResult<BookListItemVO>> list(@RequestParam(required = false) Integer page,
                                                   @RequestParam(required = false) Integer pageSize,
                                                   @RequestParam(required = false) Long categoryId,
                                                   @RequestParam(required = false) String keyword) {
        return Result.success(bookService.listFront(page, pageSize, categoryId, keyword));
    }

    @GetMapping("/{id}")
    public Result<BookDetailVO> detail(@PathVariable Long id) {
        return Result.success(bookService.getFrontDetail(id));
    }
}
