package top.wjc.bookmallbackend.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class BookUpdateRequest {

    @Min(value = 0, message = "版本号不能为负数")
    private Integer version;

    @NotBlank(message = "书名不能为空")
    private String bookName;

    private String author;

    private String publisher;

    private String isbn;

    @NotNull(message = "分类不能为空")
    private Long categoryId;

    @NotNull(message = "价格不能为空")
    @DecimalMin(value = "0.01", message = "价格必须大于0")
    private BigDecimal price;

    @NotNull(message = "库存不能为空")
    @Min(value = 0, message = "库存不能为负数")
    private Integer stock;

    @Pattern(regexp = "^$|^https?://.*\\.(jpg|jpeg|png|webp)(\\?.*)?$", message = "封面图片格式必须为jpg/png/webp")
    private String coverImage;

    private String description;

    @NotNull(message = "状态不能为空")
    private Integer status;
}
