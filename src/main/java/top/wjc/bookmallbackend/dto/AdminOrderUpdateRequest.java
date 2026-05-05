package top.wjc.bookmallbackend.dto;

import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
/**
 * 后台修改订单请求对象。
 *
 * <p>主要用于修改订单地址或推进订单状态。
 */
public class AdminOrderUpdateRequest {

    @Min(value = 0, message = "版本号不能为负数")
    private Integer version;

    private Integer status;

    private Long addressId;
}
