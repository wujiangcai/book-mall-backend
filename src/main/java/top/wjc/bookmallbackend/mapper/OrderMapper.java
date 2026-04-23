package top.wjc.bookmallbackend.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import top.wjc.bookmallbackend.entity.Order;
import top.wjc.bookmallbackend.vo.AdminOrderListItemVO;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface OrderMapper {
    int countPendingShipmentByAddressId(@Param("addressId") Long addressId);

    int insert(Order order);

    Order selectById(@Param("id") Long id);

    Order selectByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    Order selectByOrderNo(@Param("orderNo") String orderNo);

    List<Order> selectByUserId(@Param("userId") Long userId,
                               @Param("offset") int offset,
                               @Param("pageSize") int pageSize);

    long countByUserId(@Param("userId") Long userId);

    List<AdminOrderListItemVO> selectAdminList(@Param("offset") int offset,
                                               @Param("pageSize") int pageSize,
                                               @Param("status") Integer status,
                                               @Param("orderNo") String orderNo,
                                               @Param("userId") Long userId);

    long countAdminList(@Param("status") Integer status,
                        @Param("orderNo") String orderNo,
                        @Param("userId") Long userId);

    int updateStatus(@Param("id") Long id,
                     @Param("status") Integer status,
                     @Param("version") Integer version);

    int updatePaymentSuccess(@Param("id") Long id,
                             @Param("fromStatus") Integer fromStatus,
                             @Param("toStatus") Integer toStatus,
                             @Param("payTime") LocalDateTime payTime,
                             @Param("tradeNo") String tradeNo,
                             @Param("version") Integer version);

    int updatePayTime(@Param("id") Long id,
                      @Param("payTime") LocalDateTime payTime,
                      @Param("version") Integer version);

    int updateTradeNo(@Param("id") Long id,
                      @Param("tradeNo") String tradeNo,
                      @Param("version") Integer version);

    int updateShipTime(@Param("id") Long id,
                       @Param("shipTime") LocalDateTime shipTime,
                       @Param("version") Integer version);

    int updateAddress(@Param("id") Long id,
                      @Param("addressId") Long addressId,
                      @Param("version") Integer version);

    int updateShipment(@Param("id") Long id,
                       @Param("fromStatus") Integer fromStatus,
                       @Param("toStatus") Integer toStatus,
                       @Param("shipTime") LocalDateTime shipTime,
                       @Param("version") Integer version);

    int countByAddressId(@Param("addressId") Long addressId);
}
