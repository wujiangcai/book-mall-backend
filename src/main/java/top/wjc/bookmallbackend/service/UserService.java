package top.wjc.bookmallbackend.service;

import top.wjc.bookmallbackend.dto.LoginRequest;
import top.wjc.bookmallbackend.dto.RegisterRequest;
import top.wjc.bookmallbackend.dto.UpdateUserRequest;
import top.wjc.bookmallbackend.vo.AuthResponse;
import top.wjc.bookmallbackend.vo.UserInfoResponse;

public interface UserService {
    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request, boolean adminOnly);

    UserInfoResponse getUserInfo(Long userId);

    void updateUserInfo(Long userId, UpdateUserRequest request);
}
