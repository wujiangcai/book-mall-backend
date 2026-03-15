package top.wjc.bookmallbackend.service.impl;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.wjc.bookmallbackend.common.PageResult;
import top.wjc.bookmallbackend.constant.CommonStatus;
import top.wjc.bookmallbackend.constant.UserRole;
import top.wjc.bookmallbackend.dto.AdminUserCreateRequest;
import top.wjc.bookmallbackend.dto.AdminUserUpdateRequest;
import top.wjc.bookmallbackend.dto.ChangePasswordRequest;
import top.wjc.bookmallbackend.dto.LoginRequest;
import top.wjc.bookmallbackend.dto.RegisterRequest;
import top.wjc.bookmallbackend.dto.UpdateUserRequest;
import top.wjc.bookmallbackend.dto.UserStatusRequest;
import top.wjc.bookmallbackend.entity.User;
import top.wjc.bookmallbackend.exception.BusinessException;
import top.wjc.bookmallbackend.exception.ForbiddenException;
import top.wjc.bookmallbackend.exception.InvalidStatusException;
import top.wjc.bookmallbackend.exception.NotFoundException;
import top.wjc.bookmallbackend.exception.UnauthorizedException;
import top.wjc.bookmallbackend.mapper.UserMapper;
import top.wjc.bookmallbackend.service.UserService;
import top.wjc.bookmallbackend.util.JwtUtil;
import top.wjc.bookmallbackend.vo.AdminUserDetailVO;
import top.wjc.bookmallbackend.vo.AdminUserListItemVO;
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

    @Override
    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new NotFoundException();
        }
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new UnauthorizedException();
        }
        userMapper.updatePassword(userId, passwordEncoder.encode(request.getNewPassword()));
    }

    @Override
    public PageResult<AdminUserListItemVO> listAdmin(Integer page, Integer pageSize, String keyword) {
        int currentPage = normalizePage(page);
        int size = normalizeSize(pageSize);
        int offset = (currentPage - 1) * size;
        long total = userMapper.countAdminList(keyword);
        return new PageResult<>(total, userMapper.selectAdminList(offset, size, keyword), currentPage, size);
    }

    @Override
    public AdminUserDetailVO detailAdmin(Long userId) {
        AdminUserDetailVO detail = userMapper.selectAdminDetail(userId);
        if (detail == null) {
            throw new NotFoundException();
        }
        return detail;
    }

    @Override
    @Transactional
    public void createAdmin(AdminUserCreateRequest request) {
        validateUserRole(request.getRole());
        validateUserStatus(request.getStatus());
        if (userMapper.countByUsername(request.getUsername()) > 0) {
            throw new BusinessException(400, "用户名已存在");
        }
        if (request.getPhone() != null && !request.getPhone().isBlank() && userMapper.countByPhone(request.getPhone()) > 0) {
            throw new BusinessException(400, "手机号已存在");
        }
        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .nickname(request.getNickname())
                .phone(request.getPhone())
                .email(request.getEmail())
                .role(request.getRole())
                .status(request.getStatus())
                .build();
        userMapper.insert(user);
    }

    @Override
    @Transactional
    public void updateAdmin(Long userId, AdminUserUpdateRequest request) {
        User existing = userMapper.findById(userId);
        if (existing == null) {
            throw new NotFoundException();
        }
        validateUserRole(request.getRole());
        validateUserStatus(request.getStatus());
        if (!existing.getUsername().equals(request.getUsername()) && userMapper.countByUsername(request.getUsername()) > 0) {
            throw new BusinessException(400, "用户名已存在");
        }
        if (request.getPhone() != null && !request.getPhone().isBlank()
                && (existing.getPhone() == null || !existing.getPhone().equals(request.getPhone()))
                && userMapper.countByPhone(request.getPhone()) > 0) {
            throw new BusinessException(400, "手机号已存在");
        }
        User user = User.builder()
                .id(userId)
                .username(request.getUsername())
                .nickname(request.getNickname())
                .phone(request.getPhone())
                .email(request.getEmail())
                .role(request.getRole())
                .status(request.getStatus())
                .build();
        userMapper.updateAdmin(user);
    }

    @Override
    @Transactional
    public void updateStatus(Long userId, UserStatusRequest request) {
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new NotFoundException();
        }
        validateUserStatus(request.getStatus());
        userMapper.updateStatus(userId, request.getStatus());
    }

    private void validateUserStatus(Integer status) {
        if (status == null) {
            throw new InvalidStatusException("状态不能为空");
        }
        if (status != CommonStatus.ENABLED.getCode() && status != CommonStatus.DISABLED.getCode()) {
            throw new InvalidStatusException("用户状态不合法");
        }
    }

    private void validateUserRole(Integer role) {
        if (role == null) {
            throw new InvalidStatusException("角色不能为空");
        }
        if (!role.equals(UserRole.ADMIN.getCode()) && !role.equals(UserRole.USER.getCode())) {
            throw new InvalidStatusException("角色不合法");
        }
    }

    private int normalizePage(Integer page) {
        return page == null || page < 1 ? 1 : page;
    }

    private int normalizeSize(Integer pageSize) {
        int size = pageSize == null ? 20 : pageSize;
        if (size < 1) {
            return 20;
        }
        return Math.min(size, 100);
    }

    private AuthResponse buildAuthResponse(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("role", user.getRole());
        String token = jwtUtil.generateToken(claims);
        return new AuthResponse(token, user.getId(), user.getUsername(), user.getRole());
    }
}
