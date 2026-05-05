package top.wjc.bookmallbackend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
/**
 * 登录请求对象。
 *
 * <p>前台用户登录和后台管理员登录共用这一套入参结构。
 */
public class LoginRequest {
    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;
}
