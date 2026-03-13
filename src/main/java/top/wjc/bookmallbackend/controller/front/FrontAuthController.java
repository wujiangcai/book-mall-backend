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

@RestController
@RequestMapping("/api/front/auth")
public class FrontAuthController {

    private final UserService userService;

    public FrontAuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public Result<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return Result.success(userService.register(request));
    }

    @PostMapping("/login")
    public Result<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return Result.success(userService.login(request, false));
    }
}
