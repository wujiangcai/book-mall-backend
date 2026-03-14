package top.wjc.bookmallbackend.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class AdminUserListItemVO {
    private Long id;
    private String username;
    private String nickname;
    private String phone;
    private String email;
    private Integer role;
    private Integer status;
    private LocalDateTime createTime;
}
