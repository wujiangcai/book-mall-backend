package top.wjc.bookmallbackend.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface OrderMapper {
    int countPendingShipmentByAddressId(@Param("addressId") Long addressId);
}
