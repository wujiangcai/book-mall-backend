package top.wjc.bookmallbackend.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import top.wjc.bookmallbackend.entity.Order;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface OrderMapper {
    int countPendingShipmentByAddressId(@Param("addressId") Long addressId);

    int insert(Order order);

    Order selectById(@Param("id") Long id);

    Order selectByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    List<Order> selectByUserId(@Param("userId") Long userId,
                               @Param("offset") int offset,
                               @Param("pageSize") int pageSize);

    long countByUserId(@Param("userId") Long userId);

    List<Order> selectAdminList(@Param("offset") int offset,
                                @Param("pageSize") int pageSize,
                                @Param("status") Integer status,
                                @Param("orderNo") String orderNo,
                                @Param("userId") Long userId);

    long countAdminList(@Param("status") Integer status,
                        @Param("orderNo") String orderNo,
                        @Param("userId") Long userId);

    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    int updatePayTime(@Param("id") Long id, @Param("payTime") LocalDateTime payTime);

    int updateShipTime(@Param("id") Long id, @Param("shipTime") LocalDateTime shipTime);

    int updateAddress(@Param("id") Long id, @Param("addressId") Long addressId);
}
