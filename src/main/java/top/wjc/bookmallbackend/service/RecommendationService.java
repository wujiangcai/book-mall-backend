package top.wjc.bookmallbackend.service;

import top.wjc.bookmallbackend.entity.OrderItem;
import top.wjc.bookmallbackend.vo.RecommendationBookVO;

import java.util.List;

public interface RecommendationService {
    List<RecommendationBookVO> recommend(Long userId, Long sceneBookId, Integer limit);

    void recordBehavior(Long userId, Long bookId, String behaviorType);

    void recordPurchase(Long userId, List<OrderItem> items);
}
