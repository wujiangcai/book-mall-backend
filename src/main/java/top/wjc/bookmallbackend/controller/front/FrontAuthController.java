package top.wjc.bookmallbackend.controller.front;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.wjc.bookmallbackend.common.Result;
import top.wjc.bookmallbackend.dto.LoginRequest;
import top.wjc.bookmallbackend.dto.RegisterRequest;
import top.wjc.bookmallbackend.service.UserService;
import top.wjc.bookmallbackend.vo.AuthResponse;

/**
 * 前台认证控制器。
 *
 * <p>负责普通用户的注册与登录，返回 JWT Token 给前端保存。
 */
@RestController
@RequestMapping("/api/front/auth")
public class FrontAuthController {

    private final UserService userService;

    public FrontAuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    /**
     * 用户注册。
     */
    public Result<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return Result.success(userService.register(request));
    }

    @PostMapping("/login")
    /**
     * 普通用户登录。
     */
    public Result<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return Result.success(userService.login(request, false));
    }
}
