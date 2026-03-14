package top.wjc.bookmallbackend.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import top.wjc.bookmallbackend.entity.OrderItem;
import top.wjc.bookmallbackend.vo.OrderItemVO;

import java.util.List;

@Mapper
public interface OrderItemMapper {
    int batchInsert(@Param("items") List<OrderItem> items);

    List<OrderItemVO> selectByOrderId(@Param("orderId") Long orderId);
}
