package top.wjc.bookmallbackend.dto;

import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UpdateUserRequest {

    private String nickname;

    @Pattern(regexp = "^$|^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$", message = "邮箱格式不正确")
    private String email;

    @Pattern(regexp = "^$|^\\d{11}$", message = "手机号格式不正确")
    private String phone;
}
