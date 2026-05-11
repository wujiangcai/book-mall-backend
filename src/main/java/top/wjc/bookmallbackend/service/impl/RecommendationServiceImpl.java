package top.wjc.bookmallbackend.service.impl;

import org.springframework.stereotype.Service;
import top.wjc.bookmallbackend.entity.OrderItem;
import top.wjc.bookmallbackend.exception.NotFoundException;
import top.wjc.bookmallbackend.mapper.BookMapper;
import top.wjc.bookmallbackend.mapper.RecommendationMapper;
import top.wjc.bookmallbackend.service.RecommendationService;
import top.wjc.bookmallbackend.service.UploadService;
import top.wjc.bookmallbackend.vo.RecommendationBookVO;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
/**
 * 混合推荐服务实现。
 *
 * <p>算法融合四路候选：协同过滤、内容相似、AI 智能重排和热门趋势。每一路先在 SQL 中得到基础分，
 * 服务层再按权重融合、去重并生成可解释推荐理由。
 */
public class RecommendationServiceImpl implements RecommendationService {

    private static final int DEFAULT_LIMIT = 8;
    private static final int MAX_LIMIT = 20;
    private static final int CANDIDATE_MULTIPLIER = 4;
    private static final double COLLABORATIVE_WEIGHT = 0.40;
    private static final double CONTENT_WEIGHT = 0.25;
    private static final double AI_WEIGHT = 0.20;
    private static final double POPULAR_WEIGHT = 0.15;

    private static final Set<String> SUPPORTED_BEHAVIORS = Set.of("view", "cart", "purchase");

    private final RecommendationMapper recommendationMapper;
    private final BookMapper bookMapper;
    private final UploadService uploadService;

    public RecommendationServiceImpl(RecommendationMapper recommendationMapper,
                                     BookMapper bookMapper,
                                     UploadService uploadService) {
        this.recommendationMapper = recommendationMapper;
        this.bookMapper = bookMapper;
        this.uploadService = uploadService;
    }

    @Override
    public List<RecommendationBookVO> recommend(Long userId, Long sceneBookId, Integer limit) {
        int finalLimit = normalizeLimit(limit);
        int candidateLimit = Math.max(finalLimit * CANDIDATE_MULTIPLIER, finalLimit);
        Map<Long, RecommendationCandidate> candidates = new LinkedHashMap<>();

        if (userId != null) {
            mergeCandidates(candidates,
                    recommendationMapper.selectCollaborativeCandidates(userId, sceneBookId, candidateLimit),
                    COLLABORATIVE_WEIGHT,
                    "协同过滤",
                    "与您兴趣相似的用户也购买了这本书");
            mergeCandidates(candidates,
                    recommendationMapper.selectContentCandidates(userId, sceneBookId, candidateLimit),
                    CONTENT_WEIGHT,
                    "内容相似",
                    "与您的浏览、加购或购买记录在分类/作者/出版社上相近");
            mergeCandidates(candidates,
                    recommendationMapper.selectAiCandidates(userId, sceneBookId, candidateLimit),
                    AI_WEIGHT,
                    "AI智能推荐",
                    "AI结合兴趣强度、时间衰减、价格带和库存健康度综合排序");
        }

        mergeCandidates(candidates,
                recommendationMapper.selectPopularCandidates(userId, sceneBookId, candidateLimit),
                POPULAR_WEIGHT,
                "热门趋势",
                userId == null ? "匿名访问时根据销量、浏览热度和库存状态智能兜底" : "近期销量和浏览热度较高，适合作为冷启动补充");

        return candidates.values().stream()
                .sorted((left, right) -> Double.compare(right.score, left.score))
                .limit(finalLimit)
                .map(this::toVO)
                .collect(Collectors.toList());
    }

    @Override
    public void recordBehavior(Long userId, Long bookId, String behaviorType) {
        if (userId == null || bookId == null || behaviorType == null || !SUPPORTED_BEHAVIORS.contains(behaviorType)) {
            return;
        }
        if (bookMapper.selectById(bookId) == null) {
            throw new NotFoundException();
        }
        recommendationMapper.insertBehavior(userId, bookId, behaviorType, behaviorScore(behaviorType));
    }

    @Override
    public void recordPurchase(Long userId, List<OrderItem> items) {
        if (userId == null || items == null || items.isEmpty()) {
            return;
        }
        for (OrderItem item : items) {
            if (item.getBookId() != null) {
                recommendationMapper.insertBehavior(userId, item.getBookId(), "purchase", behaviorScore("purchase"));
            }
        }
    }

    private void mergeCandidates(Map<Long, RecommendationCandidate> candidates,
                                 List<RecommendationBookVO> books,
                                 double weight,
                                 String strategy,
                                 String reason) {
        if (books == null || books.isEmpty()) {
            return;
        }
        double maxScore = books.stream()
                .map(RecommendationBookVO::getScore)
                .filter(score -> score != null && score > 0)
                .mapToDouble(Double::doubleValue)
                .max()
                .orElse(1.0);
        for (RecommendationBookVO book : books) {
            if (book.getId() == null) {
                continue;
            }
            double normalizedScore = ((book.getScore() == null ? 0 : book.getScore()) / maxScore) * weight * 100;
            RecommendationCandidate candidate = candidates.computeIfAbsent(book.getId(), id -> new RecommendationCandidate(book));
            candidate.score += normalizedScore;
            candidate.addStrategy(strategy, reason);
        }
    }

    private RecommendationBookVO toVO(RecommendationCandidate candidate) {
        RecommendationBookVO book = candidate.book;
        book.setCoverImage(uploadService.resolveBookCoverUrl(book.getCoverImage()));
        book.setScore(round(candidate.score));
        book.setStrategy(String.join(" + ", candidate.strategies.keySet()));
        book.setReason(String.join("；", candidate.strategies.values()));
        return book;
    }

    private int normalizeLimit(Integer limit) {
        if (limit == null || limit < 1) {
            return DEFAULT_LIMIT;
        }
        return Math.min(limit, MAX_LIMIT);
    }

    private int behaviorScore(String behaviorType) {
        return switch (behaviorType) {
            case "purchase" -> 10;
            case "cart" -> 5;
            default -> 1;
        };
    }

    private double round(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    private static class RecommendationCandidate {
        private final RecommendationBookVO book;
        private final Map<String, String> strategies = new LinkedHashMap<>();
        private double score;

        private RecommendationCandidate(RecommendationBookVO book) {
            this.book = book;
        }

        private void addStrategy(String strategy, String reason) {
            strategies.putIfAbsent(strategy, reason);
        }
    }
}