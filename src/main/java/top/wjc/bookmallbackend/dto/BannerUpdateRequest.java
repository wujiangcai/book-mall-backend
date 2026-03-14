package top.wjc.bookmallbackend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class BannerUpdateRequest {

    @NotBlank(message = "图片地址不能为空")
    @Pattern(regexp = "^https?://.*\\.(jpg|jpeg|png|webp)$", message = "图片格式必须为jpg/png/webp")
    private String imageUrl;

    private String linkUrl;

    @Min(value = 0, message = "排序不能为负数")
    private Integer sortOrder;

    @NotNull(message = "状态不能为空")
    private Integer status;
}
