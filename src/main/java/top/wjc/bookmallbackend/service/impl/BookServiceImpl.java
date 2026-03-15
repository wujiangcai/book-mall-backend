package top.wjc.bookmallbackend.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.wjc.bookmallbackend.common.PageResult;
import top.wjc.bookmallbackend.constant.BookStatus;
import top.wjc.bookmallbackend.constant.CommonStatus;
import top.wjc.bookmallbackend.dto.BookCreateRequest;
import top.wjc.bookmallbackend.dto.BookStatusRequest;
import top.wjc.bookmallbackend.dto.BookUpdateRequest;
import top.wjc.bookmallbackend.entity.Book;
import top.wjc.bookmallbackend.entity.Category;
import top.wjc.bookmallbackend.exception.BusinessException;
import top.wjc.bookmallbackend.exception.InvalidStatusException;
import top.wjc.bookmallbackend.exception.NotFoundException;
import top.wjc.bookmallbackend.mapper.BookMapper;
import top.wjc.bookmallbackend.mapper.CategoryMapper;
import top.wjc.bookmallbackend.service.BookService;
import top.wjc.bookmallbackend.vo.BookAdminListItemVO;
import top.wjc.bookmallbackend.vo.BookDetailVO;
import top.wjc.bookmallbackend.vo.BookListItemVO;

import java.math.BigDecimal;
import java.util.List;

@Service
public class BookServiceImpl implements BookService {

    private static final int FRONT_DEFAULT_SIZE = 20;
    private static final int ADMIN_DEFAULT_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;

    private final BookMapper bookMapper;
    private final CategoryMapper categoryMapper;

    public BookServiceImpl(BookMapper bookMapper, CategoryMapper categoryMapper) {
        this.bookMapper = bookMapper;
        this.categoryMapper = categoryMapper;
    }

    @Override
    public PageResult<BookListItemVO> listFront(Integer page, Integer pageSize, Long categoryId, String keyword, BigDecimal minPrice, BigDecimal maxPrice) {
        int currentPage = normalizePage(page);
        int size = normalizeSize(pageSize, FRONT_DEFAULT_SIZE);
        int offset = (currentPage - 1) * size;
        long total = bookMapper.countFrontList(categoryId, keyword, minPrice, maxPrice);
        List<BookListItemVO> list = bookMapper.selectFrontList(offset, size, categoryId, keyword, minPrice, maxPrice);
        return new PageResult<>(total, list, currentPage, size);
    }

    @Override
    public BookDetailVO getFrontDetail(Long id) {
        BookDetailVO detail = bookMapper.selectFrontDetail(id);
        if (detail == null) {
            throw new NotFoundException();
        }
        return detail;
    }

    @Override
    public PageResult<BookAdminListItemVO> listAdmin(Integer page, Integer pageSize, Long categoryId, String keyword) {
        int currentPage = normalizePage(page);
        int size = normalizeSize(pageSize, ADMIN_DEFAULT_SIZE);
        int offset = (currentPage - 1) * size;
        long total = bookMapper.countAdminList(categoryId, keyword);
        List<BookAdminListItemVO> list = bookMapper.selectAdminList(offset, size, categoryId, keyword);
        return new PageResult<>(total, list, currentPage, size);
    }

    @Override
    @Transactional
    public void create(BookCreateRequest request) {
        validateCategory(request.getCategoryId());
        validateBookStatus(request.getStatus());
        validateIsbnDuplicate(request.getIsbn(), null);
        Book book = Book.builder()
                .bookName(request.getBookName())
                .author(request.getAuthor())
                .publisher(request.getPublisher())
                .isbn(request.getIsbn())
                .categoryId(request.getCategoryId())
                .price(request.getPrice())
                .stock(request.getStock())
                .coverImage(request.getCoverImage())
                .description(request.getDescription())
                .status(request.getStatus())
                .build();
        bookMapper.insert(book);
    }

    @Override
    @Transactional
    public void update(Long id, BookUpdateRequest request) {
        Book existing = bookMapper.selectById(id);
        if (existing == null || isSoftDeleted(existing.getStatus())) {
            throw new NotFoundException();
        }
        validateCategory(request.getCategoryId());
        validateBookStatus(request.getStatus());
        validateIsbnDuplicate(request.getIsbn(), id);
        Book book = Book.builder()
                .id(id)
                .bookName(request.getBookName())
                .author(request.getAuthor())
                .publisher(request.getPublisher())
                .isbn(request.getIsbn())
                .categoryId(request.getCategoryId())
                .price(request.getPrice())
                .stock(request.getStock())
                .coverImage(request.getCoverImage())
                .description(request.getDescription())
                .status(request.getStatus())
                .build();
        bookMapper.update(book);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Book existing = bookMapper.selectById(id);
        if (existing == null || isSoftDeleted(existing.getStatus())) {
            throw new NotFoundException();
        }
        bookMapper.softDelete(id);
    }

    @Override
    @Transactional
    public void updateStatus(Long id, BookStatusRequest request) {
        Book existing = bookMapper.selectById(id);
        if (existing == null || isSoftDeleted(existing.getStatus())) {
            throw new NotFoundException();
        }
        validateBookStatus(request.getStatus());
        bookMapper.updateStatus(id, request.getStatus());
    }

    private void validateCategory(Long categoryId) {
        Category category = categoryMapper.selectById(categoryId);
        if (category == null || isDisabled(category.getStatus()) || isSoftDeleted(category.getStatus())) {
            throw new BusinessException(400, "分类不可用");
        }
    }

    private void validateIsbnDuplicate(String isbn, Long currentId) {
        if (isbn == null || isbn.isBlank()) {
            return;
        }
        if (currentId == null) {
            if (bookMapper.countByIsbn(isbn) > 0) {
                throw new BusinessException(400, "ISBN 已存在，请确认是否为不同版本");
            }
            return;
        }
        Book existing = bookMapper.selectById(currentId);
        if (existing == null || isSoftDeleted(existing.getStatus())) {
            return;
        }
        if (!isbn.equals(existing.getIsbn()) && bookMapper.countByIsbn(isbn) > 0) {
            throw new BusinessException(400, "ISBN 已存在，请确认是否为不同版本");
        }
    }

    private boolean isSoftDeleted(Integer status) {
        return status != null && status == -1;
    }

    private boolean isDisabled(Integer status) {
        return status != null && status.equals(CommonStatus.DISABLED.getCode());
    }

    private void validateBookStatus(Integer status) {
        if (status == null) {
            throw new InvalidStatusException("状态不能为空");
        }
        if (status != BookStatus.ON_SHELF.getCode() && status != BookStatus.OFF_SHELF.getCode()) {
            throw new InvalidStatusException("图书状态不合法");
        }
    }

    private int normalizePage(Integer page) {
        return page == null || page < 1 ? 1 : page;
    }

    private int normalizeSize(Integer pageSize, int defaultSize) {
        int size = pageSize == null ? defaultSize : pageSize;
        if (size < 1) {
            return defaultSize;
        }
        return Math.min(size, MAX_PAGE_SIZE);
    }
}
