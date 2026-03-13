package top.wjc.bookmallbackend.service.impl;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.wjc.bookmallbackend.constant.CommonStatus;
import top.wjc.bookmallbackend.constant.UserRole;
import top.wjc.bookmallbackend.dto.LoginRequest;
import top.wjc.bookmallbackend.dto.RegisterRequest;
import top.wjc.bookmallbackend.dto.UpdateUserRequest;
import top.wjc.bookmallbackend.entity.User;
import top.wjc.bookmallbackend.exception.BusinessException;
import top.wjc.bookmallbackend.exception.ForbiddenException;
import top.wjc.bookmallbackend.exception.NotFoundException;
import top.wjc.bookmallbackend.exception.UnauthorizedException;
import top.wjc.bookmallbackend.mapper.UserMapper;
import top.wjc.bookmallbackend.service.UserService;
import top.wjc.bookmallbackend.util.JwtUtil;
import top.wjc.bookmallbackend.vo.AuthResponse;
import top.wjc.bookmallbackend.vo.UserInfoResponse;

import java.util.HashMap;
import java.util.Map;

@Service
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public UserServiceImpl(UserMapper userMapper, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userMapper.countByUsername(request.getUsername()) > 0) {
            throw new BusinessException(400, "用户名已存在");
        }
        if (userMapper.countByPhone(request.getPhone()) > 0) {
            throw new BusinessException(400, "手机号已存在");
        }
        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .nickname(request.getNickname())
                .phone(request.getPhone())
                .email(request.getEmail())
                .role(UserRole.USER.getCode())
                .status(CommonStatus.ENABLED.getCode())
                .build();
        userMapper.insert(user);
        return buildAuthResponse(userMapper.findByUsername(user.getUsername()));
    }

    @Override
    public AuthResponse login(LoginRequest request, boolean adminOnly) {
        User user = userMapper.findByUsername(request.getUsername());
        if (user == null) {
            throw new UnauthorizedException();
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UnauthorizedException();
        }
        if (user.getStatus() != null && user.getStatus().equals(CommonStatus.DISABLED.getCode())) {
            throw new ForbiddenException();
        }
        if (adminOnly && !user.getRole().equals(UserRole.ADMIN.getCode())) {
            throw new ForbiddenException();
        }
        return buildAuthResponse(user);
    }

    @Override
    public UserInfoResponse getUserInfo(Long userId) {
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new NotFoundException();
        }
        return new UserInfoResponse(user.getId(), user.getUsername(), user.getNickname(), user.getPhone(), user.getEmail());
    }

    @Override
    public void updateUserInfo(Long userId, UpdateUserRequest request) {
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new NotFoundException();
        }
        user.setNickname(request.getNickname());
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        userMapper.updateProfile(user);
    }

    private AuthResponse buildAuthResponse(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("role", user.getRole());
        String token = jwtUtil.generateToken(claims);
        return new AuthResponse(token, user.getId(), user.getUsername(), user.getRole());
    }
}
