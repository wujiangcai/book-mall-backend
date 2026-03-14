package top.wjc.bookmallbackend.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import top.wjc.bookmallbackend.entity.Book;
import top.wjc.bookmallbackend.vo.BookAdminListItemVO;
import top.wjc.bookmallbackend.vo.BookDetailVO;
import top.wjc.bookmallbackend.vo.BookListItemVO;

import java.util.List;

@Mapper
public interface BookMapper {

    List<BookListItemVO> selectFrontList(@Param("offset") int offset,
                                         @Param("pageSize") int pageSize,
                                         @Param("categoryId") Long categoryId,
                                         @Param("keyword") String keyword);

    long countFrontList(@Param("categoryId") Long categoryId,
                        @Param("keyword") String keyword);

    BookDetailVO selectFrontDetail(@Param("id") Long id);

    List<BookAdminListItemVO> selectAdminList(@Param("offset") int offset,
                                              @Param("pageSize") int pageSize,
                                              @Param("categoryId") Long categoryId,
                                              @Param("keyword") String keyword);

    long countAdminList(@Param("categoryId") Long categoryId,
                        @Param("keyword") String keyword);

    Book selectById(@Param("id") Long id);

    int insert(Book book);

    int update(Book book);

    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    int softDelete(@Param("id") Long id);

    int countByIsbn(@Param("isbn") String isbn);

    int countByCategoryId(@Param("categoryId") Long categoryId);
}
