package top.wjc.bookmallbackend.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class BookStatusRequest {

    @Min(value = 0, message = "版本号不能为负数")
    private Integer version;

    @NotNull(message = "状态不能为空")
    private Integer status;
}
