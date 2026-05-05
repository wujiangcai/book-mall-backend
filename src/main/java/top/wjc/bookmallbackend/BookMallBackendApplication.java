package top.wjc.bookmallbackend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.web.filter.CharacterEncodingFilter;

@MapperScan("top.wjc.bookmallbackend.mapper")
@SpringBootApplication
/**
 * 项目启动入口。
 *
 * <p>这个类只做两件事：
 * <p>1. 启动 Spring Boot 容器；
 * <p>2. 扫描 MyBatis Mapper，并注册全局 UTF-8 编码过滤器，避免中文请求或响应出现乱码。
 */
public class BookMallBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(BookMallBackendApplication.class, args);
    }

    @Bean
    /**
     * 强制 Web 请求与响应统一使用 UTF-8。
     * 这是中文项目里非常常见的一层兜底配置。
     */
    public FilterRegistrationBean<CharacterEncodingFilter> utf8CharacterEncodingFilter() {
        CharacterEncodingFilter filter = new CharacterEncodingFilter();
        filter.setEncoding("UTF-8");
        filter.setForceEncoding(true);
        FilterRegistrationBean<CharacterEncodingFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        registration.setName("utf8CharacterEncodingFilter");
        return registration;
    }

}
