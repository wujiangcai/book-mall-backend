package top.wjc.bookmallbackend.interceptor;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import top.wjc.bookmallbackend.constant.UserRole;
import top.wjc.bookmallbackend.exception.ForbiddenException;
import top.wjc.bookmallbackend.exception.UnauthorizedException;
import top.wjc.bookmallbackend.util.JwtUtil;

@Component
public class AdminAuthInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;

    public AdminAuthInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String token = request.getHeader("Authorization");
        if (token == null || token.isBlank()) {
            throw new UnauthorizedException();
        }
        try {
            Claims claims = jwtUtil.parseToken(token);
            Object role = claims.get("role");
            if (role == null || Integer.parseInt(role.toString()) != UserRole.ADMIN.getCode()) {
                throw new ForbiddenException();
            }
            request.setAttribute("userId", claims.get("userId"));
            request.setAttribute("role", role);
            return true;
        } catch (JwtException exception) {
            throw new UnauthorizedException();
        }
    }
}
