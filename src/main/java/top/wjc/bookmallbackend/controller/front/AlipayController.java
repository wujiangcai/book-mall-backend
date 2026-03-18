package top.wjc.bookmallbackend.controller.front;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.wjc.bookmallbackend.service.OrderService;

import org.springframework.beans.factory.annotation.Value;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/front/pay/alipay")
public class AlipayController {

    private final OrderService orderService;

    @Value("${alipay.frontend-base-url}")
    private String frontendBaseUrl;

    public AlipayController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/notify")
    public String notify(HttpServletRequest request) {
        Map<String, String> params = extractParams(request);
        boolean success = orderService.handleAlipayNotify(params);
        return success ? "success" : "fail";
    }

    @GetMapping("/return")
    public ResponseEntity<Void> returnUrl(HttpServletRequest request) {
        Map<String, String> params = extractParams(request);
        Long orderId = orderService.handleAlipayReturn(params);
        String redirectUrl = orderId == null ? frontendBaseUrl + "/orders" : frontendBaseUrl + "/order/" + orderId;
        return ResponseEntity.status(302)
                .header("Location", redirectUrl)
                .build();
    }

    private Map<String, String> extractParams(HttpServletRequest request) {
        Map<String, String> params = new HashMap<>();
        Map<String, String[]> parameterMap = request.getParameterMap();
        for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
            String name = entry.getKey();
            String[] values = entry.getValue();
            if (values == null || values.length == 0) {
                continue;
            }
            params.put(name, values[0]);
        }
        return params;
    }
}
