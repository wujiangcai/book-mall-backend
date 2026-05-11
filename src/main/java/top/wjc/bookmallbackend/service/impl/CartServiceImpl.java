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
import top.wjc.bookmallbackend.service.RecommendationService;
import top.wjc.bookmallbackend.service.UploadService;
import top.wjc.bookmallbackend.vo.CartItemVO;

import java.util.List;
import java.util.stream.Collectors;

@Service
/**
 * 购物车服务实现。
 *
 * <p>职责很明确：查询购物车、加入购物车、修改数量、删除购物车项。
 * 真正提交为订单之前，先在这里完成基础库存校验。
 */
public class CartServiceImpl implements CartService {

    private final CartMapper cartMapper;
    private final BookMapper bookMapper;
    private final UploadService uploadService;
    private final RecommendationService recommendationService;

    public CartServiceImpl(CartMapper cartMapper,
                           BookMapper bookMapper,
                           UploadService uploadService,
                           RecommendationService recommendationService) {
        this.cartMapper = cartMapper;
        this.bookMapper = bookMapper;
        this.uploadService = uploadService;
        this.recommendationService = recommendationService;
    }

    @Override
    /**
     * 查询当前用户购物车，并把封面图片地址补全成可直接访问的 URL。
     */
    public List<CartItemVO> list(Long userId) {
        return cartMapper.selectByUserId(userId).stream()
                .map(item -> new CartItemVO(
                        item.getId(),
                        item.getBookId(),
                        item.getBookName(),
                        item.getPrice(),
                        item.getQuantity(),
                        item.getStock(),
                        uploadService.resolveBookCoverUrl(item.getCoverImage()),
                        item.getTotalPrice()
                ))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    /**
     * 加入购物车。
     *
     * <p>核心校验：图书必须存在、未软删除、处于上架状态，并且库存足够。
     * 如果购物车里已经存在同一本书，则做数量累加而不是重复插入。
     */
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
            recommendationService.recordBehavior(userId, request.getBookId(), "cart");
            return;
        }
        cartMapper.updateQuantity(existing.getId(), totalQuantity);
        recommendationService.recordBehavior(userId, request.getBookId(), "cart");
    }

    @Override
    @Transactional
    /**
     * 修改购物车中某一项的购买数量。
     */
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
    /**
     * 删除购物车项。
     */
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
