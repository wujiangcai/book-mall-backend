package top.wjc.bookmallbackend.interceptor;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import top.wjc.bookmallbackend.exception.UnauthorizedException;
import top.wjc.bookmallbackend.util.JwtUtil;

@Component
/**
 * 前台用户鉴权拦截器。
 *
 * <p>作用：从请求头 Authorization 中读取 JWT，解析出 userId 与 role，
 * 再塞回 request attribute，供 Controller 直接使用。
 */
public class UserAuthInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;

    public UserAuthInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 预检请求直接放行，避免跨域场景下 OPTIONS 被误拦截。
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String token = request.getHeader("Authorization");
        if (token == null || token.isBlank()) {
            throw new UnauthorizedException();
        }
        try {
            Claims claims = jwtUtil.parseToken(token);
            // 解析后的用户信息挂到 request 上，避免每个控制器重复解 token。
            request.setAttribute("userId", claims.get("userId"));
            request.setAttribute("role", claims.get("role"));
            return true;
        } catch (JwtException exception) {
            throw new UnauthorizedException();
        }
    }
}
