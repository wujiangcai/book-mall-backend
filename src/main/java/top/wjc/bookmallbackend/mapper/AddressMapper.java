package top.wjc.bookmallbackend.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import top.wjc.bookmallbackend.entity.Address;

import java.util.List;

/**
 * 地址数据访问接口。
 *
 * <p>负责用户地址的增删改查，以及默认地址相关操作。
 */
@Mapper
public interface AddressMapper {

    List<Address> selectByUserId(@Param("userId") Long userId);

    Address selectById(@Param("id") Long id);

    int insert(Address address);

    int update(Address address);

    int updateDefault(Address address);

    int deleteById(@Param("id") Long id);

    int clearDefaultByUserId(@Param("userId") Long userId);

    int countDefaultByUserId(@Param("userId") Long userId);

    Address selectLatestByUserId(@Param("userId") Long userId);
}
