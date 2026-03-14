package top.wjc.bookmallbackend.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import top.wjc.bookmallbackend.entity.Banner;
import top.wjc.bookmallbackend.vo.BannerVO;

import java.util.List;

@Mapper
public interface BannerMapper {

    List<Banner> selectAdminList();

    List<BannerVO> selectFrontList();

    Banner selectById(@Param("id") Long id);

    int insert(Banner banner);

    int update(Banner banner);

    int updateSort(@Param("id") Long id, @Param("sortOrder") Integer sortOrder);

    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    int softDelete(@Param("id") Long id);
}
