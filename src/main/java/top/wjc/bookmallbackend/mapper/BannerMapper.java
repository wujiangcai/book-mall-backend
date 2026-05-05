package top.wjc.bookmallbackend.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import top.wjc.bookmallbackend.entity.Banner;

import java.util.List;

/**
 * 轮播图数据访问接口。
 *
 * <p>负责轮播图的前台查询、后台分页查询和维护操作。
 */
@Mapper
public interface BannerMapper {

    List<Banner> selectAdminList();

    List<Banner> selectAdminListPaged(@Param("offset") int offset, @Param("pageSize") int pageSize);

    long countAdminList();

    List<Banner> selectFrontList();

    Banner selectById(@Param("id") Long id);

    int insert(Banner banner);

    int update(Banner banner);

    int updateSort(@Param("id") Long id, @Param("sortOrder") Integer sortOrder);

    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    int softDelete(@Param("id") Long id);
}
