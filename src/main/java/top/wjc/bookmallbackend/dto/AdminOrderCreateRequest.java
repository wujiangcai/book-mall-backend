package top.wjc.bookmallbackend.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class AdminOrderCreateRequest {

    @NotNull(message = "用户不能为空")
    private Long userId;

    @NotNull(message = "收货地址不能为空")
    private Long addressId;

    @NotEmpty(message = "请填写订单商品")
    private List<AdminOrderItemRequest> items;

    @Data
    public static class AdminOrderItemRequest {
        @NotNull(message = "图书不能为空")
        private Long bookId;

        @NotNull(message = "数量不能为空")
        private Integer quantity;
    }
}
