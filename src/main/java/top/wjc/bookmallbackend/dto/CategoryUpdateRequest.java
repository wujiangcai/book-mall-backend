package top.wjc.bookmallbackend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CategoryUpdateRequest {

    @NotBlank(message = "分类名称不能为空")
    private String categoryName;

    @Min(value = 0, message = "父分类ID不能为负数")
    private Long parentId = 0L;

    @Min(value = 0, message = "排序值不能为负数")
    private Integer sortOrder = 0;
}
