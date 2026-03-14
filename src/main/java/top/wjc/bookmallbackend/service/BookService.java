package top.wjc.bookmallbackend.service;

import top.wjc.bookmallbackend.common.PageResult;
import top.wjc.bookmallbackend.dto.BookCreateRequest;
import top.wjc.bookmallbackend.dto.BookStatusRequest;
import top.wjc.bookmallbackend.dto.BookUpdateRequest;
import top.wjc.bookmallbackend.vo.BookAdminListItemVO;
import top.wjc.bookmallbackend.vo.BookDetailVO;
import top.wjc.bookmallbackend.vo.BookListItemVO;

public interface BookService {
    PageResult<BookListItemVO> listFront(Integer page, Integer pageSize, Long categoryId, String keyword);

    BookDetailVO getFrontDetail(Long id);

    PageResult<BookAdminListItemVO> listAdmin(Integer page, Integer pageSize, Long categoryId, String keyword);

    void create(BookCreateRequest request);

    void update(Long id, BookUpdateRequest request);

    void delete(Long id);

    void updateStatus(Long id, BookStatusRequest request);
}
