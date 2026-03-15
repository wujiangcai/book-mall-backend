package top.wjc.bookmallbackend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class AdminUserUpdateRequest {

    @NotBlank(message = "用户名不能为空")
    private String username;

    private String nickname;

    @Pattern(regexp = "^$|^\\d{11}$", message = "手机号格式不正确")
    private String phone;

    @Pattern(regexp = "^$|^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$", message = "邮箱格式不正确")
    private String email;

    @NotNull(message = "角色不能为空")
    private Integer role;

    @NotNull(message = "状态不能为空")
    private Integer status;
}
