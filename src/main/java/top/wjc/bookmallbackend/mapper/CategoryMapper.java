package top.wjc.bookmallbackend.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import top.wjc.bookmallbackend.entity.Category;

import java.util.List;

@Mapper
public interface CategoryMapper {

    List<Category> selectAllEnabled();

    List<Category> selectAdminList();

    List<Category> selectAdminListPaged(@Param("offset") int offset, @Param("pageSize") int pageSize);

    long countAdminList();

    Category selectById(@Param("id") Long id);

    int countByName(@Param("categoryName") String categoryName);

    int countByNameExcludeId(@Param("categoryName") String categoryName, @Param("id") Long id);

    int insert(Category category);

    int update(Category category);

    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    int softDelete(@Param("id") Long id);
}
