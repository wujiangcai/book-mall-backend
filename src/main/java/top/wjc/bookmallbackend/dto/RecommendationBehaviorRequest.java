package top.wjc.bookmallbackend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
/**
 * 用户推荐行为上报请求。
 */
public class RecommendationBehaviorRequest {
    @NotNull(message = "图书ID不能为空")
    private Long bookId;

    @NotBlank(message = "行为类型不能为空")
    @Pattern(regexp = "view|cart|purchase", message = "行为类型只能是view、cart或purchase")
    private String behaviorType;
}
