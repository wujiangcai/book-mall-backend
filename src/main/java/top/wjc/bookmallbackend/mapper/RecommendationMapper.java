package top.wjc.bookmallbackend.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import top.wjc.bookmallbackend.vo.RecommendationBookVO;

import java.util.List;

@Mapper
/**
 * 图书推荐数据访问接口。
 *
 * <p>负责记录用户行为，并分别查询协同过滤、内容相似、AI 重排和热门趋势候选集。
 */
public interface RecommendationMapper {
    int insertBehavior(@Param("userId") Long userId,
                       @Param("bookId") Long bookId,
                       @Param("behaviorType") String behaviorType,
                       @Param("score") Integer score);

    List<RecommendationBookVO> selectCollaborativeCandidates(@Param("userId") Long userId,
                                                             @Param("sceneBookId") Long sceneBookId,
                                                             @Param("limit") int limit);

    List<RecommendationBookVO> selectContentCandidates(@Param("userId") Long userId,
                                                       @Param("sceneBookId") Long sceneBookId,
                                                       @Param("limit") int limit);

    List<RecommendationBookVO> selectAiCandidates(@Param("userId") Long userId,
                                                  @Param("sceneBookId") Long sceneBookId,
                                                  @Param("limit") int limit);

    List<RecommendationBookVO> selectPopularCandidates(@Param("userId") Long userId,
                                                       @Param("sceneBookId") Long sceneBookId,
                                                       @Param("limit") int limit);
}