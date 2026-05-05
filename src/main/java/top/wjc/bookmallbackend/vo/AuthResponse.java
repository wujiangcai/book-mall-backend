package top.wjc.bookmallbackend.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
/**
 * 登录成功响应对象。
 *
 * <p>前端拿到 token 后会保存到本地，后续请求再自动携带。
 */
public class AuthResponse {
    private String token;
    private Long userId;
    private String username;
    private Integer role;
}
