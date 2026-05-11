package top.wjc.bookmallbackend.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
/**
 * Jackson JSON 配置。
 *
 * <p>为大模型客户端和 AI 分析服务提供统一 ObjectMapper，避免不同 Spring Boot 版本下自动装配缺失。
 */
public class JacksonConfig {

    @Bean
    public ObjectMapper objectMapper() {
        return JsonMapper.builder()
                .findAndAddModules()
                .build();
    }
}