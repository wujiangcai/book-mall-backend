package top.wjc.bookmallbackend.controller.admin;

import jakarta.validation.Valid;
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
import top.wjc.bookmallbackend.dto.BookCreateRequest;
import top.wjc.bookmallbackend.dto.BookStatusRequest;
import top.wjc.bookmallbackend.dto.BookUpdateRequest;
import top.wjc.bookmallbackend.service.BookService;
import top.wjc.bookmallbackend.vo.BookAdminListItemVO;

@RestController
@RequestMapping("/api/admin/books")
public class AdminBookController {

    private final BookService bookService;

    public AdminBookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping
    public Result<PageResult<BookAdminListItemVO>> list(@RequestParam(required = false) Integer page,
                                                        @RequestParam(required = false) Integer pageSize,
                                                        @RequestParam(required = false) Long categoryId,
                                                        @RequestParam(required = false) String keyword) {
        return Result.success(bookService.listAdmin(page, pageSize, categoryId, keyword));
    }

    @PostMapping
    public Result<Void> create(@Valid @RequestBody BookCreateRequest request) {
        bookService.create(request);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody BookUpdateRequest request) {
        bookService.update(id, request);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        bookService.delete(id);
        return Result.success();
    }

    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @Valid @RequestBody BookStatusRequest request) {
        bookService.updateStatus(id, request);
        return Result.success();
    }
}
