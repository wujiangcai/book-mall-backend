package top.wjc.bookmallbackend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import top.wjc.bookmallbackend.interceptor.AdminAuthInterceptor;
import top.wjc.bookmallbackend.interceptor.UserAuthInterceptor;

@Configuration
/**
 * MVC 拦截器配置。
 *
 * <p>这里决定了哪些接口需要登录、哪些接口可以匿名访问。
 * 前台和后台分别使用不同的拦截器，便于区分普通用户权限与管理员权限。
 */
public class WebMvcConfig implements WebMvcConfigurer {

    private final UserAuthInterceptor userAuthInterceptor;
    private final AdminAuthInterceptor adminAuthInterceptor;

    public WebMvcConfig(UserAuthInterceptor userAuthInterceptor,
                        AdminAuthInterceptor adminAuthInterceptor) {
        this.userAuthInterceptor = userAuthInterceptor;
        this.adminAuthInterceptor = adminAuthInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 前台接口：除登录注册、图书浏览、分类、轮播图、支付回调外，都要求携带用户 Token。
        registry.addInterceptor(userAuthInterceptor)
                .addPathPatterns("/api/front/**")
                .excludePathPatterns("/api/front/auth/**", "/api/front/books/**", "/api/front/categories", "/api/front/banners", "/api/front/pay/**");

        // 后台接口：除管理员登录外，全部要求管理员 Token。
        registry.addInterceptor(adminAuthInterceptor)
                .addPathPatterns("/api/admin/**")
                .excludePathPatterns("/api/admin/auth/**");
    }
}
