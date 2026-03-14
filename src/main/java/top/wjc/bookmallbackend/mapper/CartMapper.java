package top.wjc.bookmallbackend.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import top.wjc.bookmallbackend.entity.Cart;
import top.wjc.bookmallbackend.vo.CartItemVO;

import java.util.List;

@Mapper
public interface CartMapper {

    List<CartItemVO> selectByUserId(@Param("userId") Long userId);

    Cart selectById(@Param("id") Long id);

    Cart selectByUserIdAndBookId(@Param("userId") Long userId, @Param("bookId") Long bookId);

    int insert(Cart cart);

    int updateQuantity(@Param("id") Long id, @Param("quantity") Integer quantity);

    int deleteById(@Param("id") Long id);

    java.util.List<Cart> selectByIdsAndUserId(@Param("cartIds") java.util.List<Long> cartIds,
                                              @Param("userId") Long userId);

    int deleteByIdsAndUserId(@Param("cartIds") java.util.List<Long> cartIds,
                             @Param("userId") Long userId);
}
