package top.wjc.bookmallbackend.controller.front;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.wjc.bookmallbackend.common.Result;
import top.wjc.bookmallbackend.dto.ChangePasswordRequest;
import top.wjc.bookmallbackend.dto.UpdateUserRequest;
import top.wjc.bookmallbackend.service.UserService;
import top.wjc.bookmallbackend.vo.UserInfoResponse;

/**
 * 前台用户中心控制器。
 *
 * <p>负责“我的信息”和“修改密码”这类与当前登录用户直接相关的操作。
 */
@RestController
@RequestMapping("/api/front/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/info")
    /**
     * 查询当前登录用户资料。
     */
    public Result<UserInfoResponse> info(HttpServletRequest request) {
        Object userId = request.getAttribute("userId");
        return Result.success(userService.getUserInfo(Long.valueOf(userId.toString())));
    }

    @PutMapping("/info")
    /**
     * 修改当前登录用户资料。
     */
    public Result<Void> update(@Valid @RequestBody UpdateUserRequest request, HttpServletRequest httpRequest) {
        Object userId = httpRequest.getAttribute("userId");
        userService.updateUserInfo(Long.valueOf(userId.toString()), request);
        return Result.success();
    }

    @PutMapping("/password")
    /**
     * 修改当前登录用户密码。
     */
    public Result<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request, HttpServletRequest httpRequest) {
        Object userId = httpRequest.getAttribute("userId");
        userService.changePassword(Long.valueOf(userId.toString()), request);
        return Result.success();
    }
}
