package top.wjc.bookmallbackend.controller.front;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.wjc.bookmallbackend.common.Result;
import top.wjc.bookmallbackend.dto.RecommendationBehaviorRequest;
import top.wjc.bookmallbackend.exception.UnauthorizedException;
import top.wjc.bookmallbackend.service.RecommendationService;
import top.wjc.bookmallbackend.util.JwtUtil;
import top.wjc.bookmallbackend.vo.RecommendationBookVO;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/front/recommendations")
/**
 * 前台个性化推荐控制器。
 *
 * <p>获取推荐允许匿名访问，登录用户会根据 Token 叠加个性化信号；行为上报仍要求登录。
 */
public class FrontRecommendationController {

    private final RecommendationService recommendationService;
    private final JwtUtil jwtUtil;

    public FrontRecommendationController(RecommendationService recommendationService, JwtUtil jwtUtil) {
        this.recommendationService = recommendationService;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping
    /**
     * 获取混合推荐结果，支持在图书详情页传入 sceneBookId 生成“看了又看”。
     */
    public Result<List<RecommendationBookVO>> list(@RequestParam(required = false) @Min(value = 1, message = "sceneBookId必须大于0") Long sceneBookId,
                                                   @RequestParam(required = false) @Min(value = 1, message = "limit必须大于0") Integer limit,
                                                   HttpServletRequest request) {
        Long userId = optionalUserId(request);
        return Result.success(recommendationService.recommend(userId, sceneBookId, limit));
    }

    @PostMapping("/behaviors")
    /**
     * 上报用户推荐行为，用于持续优化推荐结果。
     */
    public Result<Void> recordBehavior(@Valid @RequestBody RecommendationBehaviorRequest request,
                                       HttpServletRequest httpRequest) {
        Long userId = requiredUserId(httpRequest);
        recommendationService.recordBehavior(userId, request.getBookId(), request.getBehaviorType());
        return Result.success();
    }

    private Long optionalUserId(HttpServletRequest request) {
        Object userId = request.getAttribute("userId");
        if (userId != null) {
            return Long.valueOf(userId.toString());
        }
        String token = request.getHeader("Authorization");
        if (token == null || token.isBlank()) {
            return null;
        }
        try {
            Object claimUserId = jwtUtil.parseToken(token).get("userId");
            return claimUserId == null ? null : Long.valueOf(claimUserId.toString());
        } catch (JwtException | IllegalArgumentException exception) {
            return null;
        }
    }

    private Long requiredUserId(HttpServletRequest request) {
        Object userId = request.getAttribute("userId");
        if (userId == null) {
            throw new UnauthorizedException();
        }
        return Long.valueOf(userId.toString());
    }
}