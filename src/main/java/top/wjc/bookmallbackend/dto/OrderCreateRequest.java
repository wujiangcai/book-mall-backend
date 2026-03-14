package top.wjc.bookmallbackend.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class OrderCreateRequest {

    @NotNull(message = "收货地址不能为空")
    private Long addressId;

    @NotEmpty(message = "请选择购物车商品")
    private List<Long> cartIds;
}
