package top.wjc.bookmallbackend.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.wjc.bookmallbackend.properties.AiModelProperties;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
/**
 * OpenAI 兼容 Chat Completions 客户端。
 *
 * <p>OpenAI 与阿里云百炼/通义千问均支持该协议，因此这里只维护一套稳定的 HTTP 调用逻辑。
 */
public class OpenAiCompatibleClient {

    private final AiModelProperties properties;
    private final ObjectMapper objectMapper;

    public OpenAiCompatibleClient(AiModelProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    public AiChatResult chat(String systemPrompt, String userPrompt) {
        if (!properties.isEnabled() || isBlank(properties.getApiKey())) {
            return AiChatResult.disabled();
        }
        try {
            Map<String, Object> body = Map.of(
                    "model", properties.getModel(),
                    "temperature", properties.getTemperature(),
                    "max_tokens", properties.getMaxTokens(),
                    "response_format", Map.of("type", "json_object"),
                    "messages", List.of(
                            Map.of("role", "system", "content", systemPrompt),
                            Map.of("role", "user", "content", userPrompt)
                    )
            );

            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(resolveTimeoutSeconds()))
                    .build();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(resolveChatCompletionsUrl(properties.getBaseUrl())))
                    .timeout(Duration.ofSeconds(resolveTimeoutSeconds()))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + properties.getApiKey())
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                log.warn("AI model request returned non-2xx status, provider={}, model={}, status={}",
                        properties.getProvider(), properties.getModel(), response.statusCode());
                return AiChatResult.failed();
            }
            return extractContent(response.body());
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            log.warn("AI model request interrupted, provider={}, model={}", properties.getProvider(), properties.getModel());
            return AiChatResult.timeout();
        } catch (java.net.http.HttpTimeoutException exception) {
            log.warn("AI model request timed out, provider={}, model={}, timeoutSeconds={}",
                    properties.getProvider(), properties.getModel(), resolveTimeoutSeconds());
            return AiChatResult.timeout();
        } catch (Exception exception) {
            log.warn("AI model request failed, provider={}, model={}, message={}",
                    properties.getProvider(), properties.getModel(), exception.getMessage());
            return AiChatResult.failed();
        }
    }

    private AiChatResult extractContent(String responseBody) throws Exception {
        if (isBlank(responseBody)) {
            return AiChatResult.failed();
        }
        JsonNode root = objectMapper.readTree(responseBody);
        JsonNode contentNode = root.path("choices").path(0).path("message").path("content");
        if (contentNode.isMissingNode() || contentNode.isNull() || isBlank(contentNode.asText())) {
            return AiChatResult.failed();
        }
        return AiChatResult.success(contentNode.asText());
    }

    private String resolveChatCompletionsUrl(String baseUrl) {
        String normalized = normalizeBaseUrl(baseUrl);
        if (normalized.endsWith("/chat/completions") || normalized.endsWith("/responses")) {
            return normalized;
        }
        return normalized + "/chat/completions";
    }

    private String normalizeBaseUrl(String baseUrl) {
        String normalized = isBlank(baseUrl) ? "https://api.openai.com/v1" : baseUrl.trim();
        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }

    private int resolveTimeoutSeconds() {
        Integer timeoutSeconds = properties.getTimeoutSeconds();
        if (timeoutSeconds == null || timeoutSeconds < 3) {
            return 20;
        }
        return Math.min(timeoutSeconds, 180);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    @Getter
    @AllArgsConstructor
    public static class AiChatResult {
        private final boolean success;
        private final boolean timeout;
        private final String content;

        public static AiChatResult success(String content) {
            return new AiChatResult(true, false, content);
        }

        public static AiChatResult timeout() {
            return new AiChatResult(false, true, null);
        }

        public static AiChatResult failed() {
            return new AiChatResult(false, false, null);
        }

        public static AiChatResult disabled() {
            return new AiChatResult(false, false, null);
        }
    }
}