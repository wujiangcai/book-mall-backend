package top.wjc.bookmallbackend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CategoryStatusRequest {

    @NotNull(message = "状态不能为空")
    private Integer status;
}
