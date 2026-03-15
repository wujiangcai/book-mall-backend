package top.wjc.bookmallbackend.service;

import top.wjc.bookmallbackend.common.PageResult;
import top.wjc.bookmallbackend.dto.ChangePasswordRequest;
import top.wjc.bookmallbackend.dto.LoginRequest;
import top.wjc.bookmallbackend.dto.RegisterRequest;
import top.wjc.bookmallbackend.dto.UpdateUserRequest;
import top.wjc.bookmallbackend.dto.UserStatusRequest;
import top.wjc.bookmallbackend.vo.AdminUserDetailVO;
import top.wjc.bookmallbackend.vo.AdminUserListItemVO;
import top.wjc.bookmallbackend.vo.AuthResponse;
import top.wjc.bookmallbackend.vo.UserInfoResponse;

public interface UserService {
    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request, boolean adminOnly);

    UserInfoResponse getUserInfo(Long userId);

    void updateUserInfo(Long userId, UpdateUserRequest request);

    void changePassword(Long userId, ChangePasswordRequest request);

    PageResult<AdminUserListItemVO> listAdmin(Integer page, Integer pageSize, String keyword);

    AdminUserDetailVO detailAdmin(Long userId);

    void updateStatus(Long userId, UserStatusRequest request);
}
