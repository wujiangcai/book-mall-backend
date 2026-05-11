package top.wjc.bookmallbackend.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "ai.model")
/**
 * 大模型调用配置。
 *
 * <p>采用 OpenAI Chat Completions 兼容协议，可通过 baseUrl/model/apiKey 在 ChatGPT、通义千问等模型间切换。
 */
public class AiModelProperties {
    private boolean enabled = false;
    private String provider = "openai";
    private String baseUrl = "https://api.openai.com/v1";
    private String apiKey = "";
    private String model = "gpt-4o-mini";
    private Double temperature = 0.2;
    private Integer maxTokens = 1400;
    private Integer timeoutSeconds = 20;
}