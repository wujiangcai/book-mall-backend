package top.wjc.bookmallbackend.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
/**
 * 前台创建订单请求对象。
 *
 * <p>用户通过地址 ID 和购物车 ID 列表提交下单请求。
 */
public class OrderCreateRequest {

    @NotNull(message = "收货地址不能为空")
    private Long addressId;

    @NotEmpty(message = "请选择购物车商品")
    private List<Long> cartIds;
}
