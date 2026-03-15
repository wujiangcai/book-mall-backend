package top.wjc.bookmallbackend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AdminOrderUpdateRequest {

    @NotNull(message = "状态不能为空")
    private Integer status;

    @NotNull(message = "收货地址不能为空")
    private Long addressId;
}
