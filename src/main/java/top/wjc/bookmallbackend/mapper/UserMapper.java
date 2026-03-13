package top.wjc.bookmallbackend.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import top.wjc.bookmallbackend.entity.User;

@Mapper
public interface UserMapper {

    User findByUsername(@Param("username") String username);

    User findById(@Param("id") Long id);

    int countByUsername(@Param("username") String username);

    int countByPhone(@Param("phone") String phone);

    int insert(User user);

    int updateProfile(User user);
}
