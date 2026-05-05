package top.wjc.bookmallbackend.controller.admin;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.wjc.bookmallbackend.common.Result;
import top.wjc.bookmallbackend.dto.LoginRequest;
import top.wjc.bookmallbackend.service.UserService;
import top.wjc.bookmallbackend.vo.AuthResponse;

/**
 * 后台认证控制器。
 *
 * <p>与前台登录共用同一套用户表，但这里会强制要求登录账号具备管理员角色。
 */
@RestController
@RequestMapping("/api/admin/auth")
public class AdminAuthController {

    private final UserService userService;

    public AdminAuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    /**
     * 管理员登录。
     */
    public Result<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return Result.success(userService.login(request, true));
    }
}
