package top.wjc.bookmallbackend.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
/**
 * 后台代客创建订单请求对象。
 *
 * <p>管理员可以直接指定用户、地址以及商品明细来创建订单。
 */
public class AdminOrderCreateRequest {

    @NotNull(message = "用户不能为空")
    private Long userId;

    @NotNull(message = "收货地址不能为空")
    private Long addressId;

    @NotEmpty(message = "请填写订单商品")
    private List<AdminOrderItemRequest> items;

    @Data
    /**
     * 后台创建订单时的单个商品项。
     */
    public static class AdminOrderItemRequest {
        @NotNull(message = "图书不能为空")
        private Long bookId;

        @NotNull(message = "数量不能为空")
        private Integer quantity;
    }
}
