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

@RestController
@RequestMapping("/api/admin/auth")
public class AdminAuthController {

    private final UserService userService;

    public AdminAuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    public Result<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return Result.success(userService.login(request, true));
    }
}
