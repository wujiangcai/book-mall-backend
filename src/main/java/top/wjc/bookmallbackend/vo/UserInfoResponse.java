package top.wjc.bookmallbackend.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
/**
 * 当前登录用户信息响应对象。
 */
public class UserInfoResponse {
    private Long id;
    private String username;
    private String nickname;
    private String phone;
    private String email;
}
