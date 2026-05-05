package top.wjc.bookmallbackend.controller.admin;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.wjc.bookmallbackend.common.PageResult;
import top.wjc.bookmallbackend.common.Result;
import top.wjc.bookmallbackend.dto.AdminUserCreateRequest;
import top.wjc.bookmallbackend.dto.AdminUserUpdateRequest;
import top.wjc.bookmallbackend.dto.ChangePasswordRequest;
import top.wjc.bookmallbackend.dto.UserStatusRequest;
import top.wjc.bookmallbackend.service.UserService;
import top.wjc.bookmallbackend.vo.AdminAddressVO;
import top.wjc.bookmallbackend.vo.AdminUserDetailVO;
import top.wjc.bookmallbackend.vo.AdminUserListItemVO;

import java.util.List;

/**
 * 后台用户管理控制器。
 *
 * <p>管理员可以在这里查看用户列表、详情、地址，以及执行启停用、重置密码等管理动作。
 */
@Validated
@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private final UserService userService;

    public AdminUserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    /**
     * 后台分页查询用户。
     */
    public Result<PageResult<AdminUserListItemVO>> list(@RequestParam(required = false) @Min(value = 1, message = "page必须大于0") Integer page,
                                                        @RequestParam(required = false) @Min(value = 1, message = "pageSize必须大于0") Integer pageSize,
                                                        @RequestParam(required = false) String keyword) {
        return Result.success(userService.listAdmin(page, pageSize, keyword));
    }

    @GetMapping("/{id}")
    /**
     * 查询用户详情。
     */
    public Result<AdminUserDetailVO> detail(@PathVariable Long id) {
        return Result.success(userService.detailAdmin(id));
    }

    @GetMapping("/{id}/addresses")
    /**
     * 查询某个用户的地址列表。
     */
    public Result<List<AdminAddressVO>> addresses(@PathVariable Long id) {
        return Result.success(userService.listAddressesAdmin(id));
    }

    @PostMapping
    /**
     * 后台新增用户或管理员账号。
     */
    public Result<Void> create(@Valid @RequestBody AdminUserCreateRequest request) {
        userService.createAdmin(request);
        return Result.success();
    }

    @PutMapping("/{id}")
    /**
     * 修改用户资料与角色状态。
     */
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody AdminUserUpdateRequest request) {
        userService.updateAdmin(id, request);
        return Result.success();
    }

    @PutMapping("/{id}/status")
    /**
     * 修改用户启用状态。
     */
    public Result<Void> updateStatus(@PathVariable Long id, @Valid @RequestBody UserStatusRequest request) {
        userService.updateStatus(id, request);
        return Result.success();
    }

    @PutMapping("/{id}/reset-password")
    /**
     * 后台重置用户密码为默认值。
     */
    public Result<Void> resetPassword(@PathVariable Long id) {
        userService.resetPasswordAdmin(id);
        return Result.success();
    }

    @PutMapping("/password")
    /**
     * 当前管理员修改自己的登录密码。
     */
    public Result<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request, HttpServletRequest httpRequest) {
        Object userId = httpRequest.getAttribute("userId");
        userService.changePassword(Long.valueOf(userId.toString()), request);
        return Result.success();
    }
}
