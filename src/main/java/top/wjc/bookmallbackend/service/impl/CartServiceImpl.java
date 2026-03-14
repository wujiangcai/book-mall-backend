package top.wjc.bookmallbackend.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.wjc.bookmallbackend.constant.BookStatus;
import top.wjc.bookmallbackend.dto.CartAddRequest;
import top.wjc.bookmallbackend.dto.CartUpdateRequest;
import top.wjc.bookmallbackend.entity.Book;
import top.wjc.bookmallbackend.entity.Cart;
import top.wjc.bookmallbackend.exception.BookOffShelfException;
import top.wjc.bookmallbackend.exception.InsufficientStockException;
import top.wjc.bookmallbackend.exception.NotFoundException;
import top.wjc.bookmallbackend.mapper.BookMapper;
import top.wjc.bookmallbackend.mapper.CartMapper;
import top.wjc.bookmallbackend.service.CartService;
import top.wjc.bookmallbackend.vo.CartItemVO;

import java.util.List;

@Service
public class CartServiceImpl implements CartService {

    private final CartMapper cartMapper;
    private final BookMapper bookMapper;

    public CartServiceImpl(CartMapper cartMapper, BookMapper bookMapper) {
        this.cartMapper = cartMapper;
        this.bookMapper = bookMapper;
    }

    @Override
    public List<CartItemVO> list(Long userId) {
        return cartMapper.selectByUserId(userId);
    }

    @Override
    @Transactional
    public void add(Long userId, CartAddRequest request) {
        Book book = bookMapper.selectById(request.getBookId());
        if (book == null || isSoftDeleted(book.getStatus())) {
            throw new NotFoundException();
        }
        if (book.getStatus() == null || book.getStatus() != BookStatus.ON_SHELF.getCode()) {
            throw new BookOffShelfException();
        }
        int totalQuantity = request.getQuantity();
        Cart existing = cartMapper.selectByUserIdAndBookId(userId, request.getBookId());
        if (existing != null) {
            totalQuantity = existing.getQuantity() + request.getQuantity();
        }
        if (book.getStock() == null || totalQuantity > book.getStock()) {
            throw new InsufficientStockException();
        }
        if (existing == null) {
            Cart cart = Cart.builder()
                    .userId(userId)
                    .bookId(request.getBookId())
                    .quantity(request.getQuantity())
                    .build();
            cartMapper.insert(cart);
            return;
        }
        cartMapper.updateQuantity(existing.getId(), totalQuantity);
    }

    @Override
    @Transactional
    public void updateQuantity(Long userId, Long cartId, CartUpdateRequest request) {
        Cart cart = cartMapper.selectById(cartId);
        if (cart == null || !cart.getUserId().equals(userId)) {
            throw new NotFoundException();
        }
        Book book = bookMapper.selectById(cart.getBookId());
        if (book == null || isSoftDeleted(book.getStatus())) {
            throw new NotFoundException();
        }
        if (book.getStatus() == null || book.getStatus() != BookStatus.ON_SHELF.getCode()) {
            throw new BookOffShelfException();
        }
        if (book.getStock() == null || request.getQuantity() > book.getStock()) {
            throw new InsufficientStockException();
        }
        cartMapper.updateQuantity(cartId, request.getQuantity());
    }

    @Override
    @Transactional
    public void delete(Long userId, Long cartId) {
        Cart cart = cartMapper.selectById(cartId);
        if (cart == null || !cart.getUserId().equals(userId)) {
            throw new NotFoundException();
        }
        cartMapper.deleteById(cartId);
    }

    private boolean isSoftDeleted(Integer status) {
        return status != null && status == -1;
    }
}
