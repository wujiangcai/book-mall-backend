package top.wjc.bookmallbackend.dto;

import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class AdminOrderUpdateRequest {

    @Min(value = 0, message = "版本号不能为负数")
    private Integer version;

    private Integer status;

    private Long addressId;
}
