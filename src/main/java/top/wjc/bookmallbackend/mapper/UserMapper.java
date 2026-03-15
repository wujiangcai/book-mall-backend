package top.wjc.bookmallbackend.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import top.wjc.bookmallbackend.entity.User;
import top.wjc.bookmallbackend.vo.AdminUserDetailVO;
import top.wjc.bookmallbackend.vo.AdminUserListItemVO;

import java.util.List;

@Mapper
public interface UserMapper {

    User findByUsername(@Param("username") String username);

    User findById(@Param("id") Long id);

    int countByUsername(@Param("username") String username);

    int countByPhone(@Param("phone") String phone);

    int insert(User user);

    int updateProfile(User user);

    int updateAdmin(User user);

    int updatePassword(@Param("id") Long id, @Param("password") String password);

    List<AdminUserListItemVO> selectAdminList(@Param("offset") int offset,
                                              @Param("pageSize") int pageSize,
                                              @Param("keyword") String keyword);

    long countAdminList(@Param("keyword") String keyword);

    AdminUserDetailVO selectAdminDetail(@Param("id") Long id);

    int updateStatus(@Param("id") Long id, @Param("status") Integer status);
}
