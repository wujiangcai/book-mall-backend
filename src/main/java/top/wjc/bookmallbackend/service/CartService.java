package top.wjc.bookmallbackend.service;

import top.wjc.bookmallbackend.dto.CartAddRequest;
import top.wjc.bookmallbackend.dto.CartUpdateRequest;
import top.wjc.bookmallbackend.vo.CartItemVO;

import java.util.List;

public interface CartService {
    List<CartItemVO> list(Long userId);

    void add(Long userId, CartAddRequest request);

    void updateQuantity(Long userId, Long cartId, CartUpdateRequest request);

    void delete(Long userId, Long cartId);
}
