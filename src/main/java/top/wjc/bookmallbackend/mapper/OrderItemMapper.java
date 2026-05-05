package top.wjc.bookmallbackend.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import top.wjc.bookmallbackend.entity.OrderItem;
import top.wjc.bookmallbackend.vo.OrderItemVO;

import java.util.List;

/**
 * 订单明细数据访问接口。
 *
 * <p>负责批量写入订单明细和按订单查询商品明细。
 */
@Mapper
public interface OrderItemMapper {
    int batchInsert(@Param("items") List<OrderItem> items);

    List<OrderItemVO> selectByOrderId(@Param("orderId") Long orderId);
}
