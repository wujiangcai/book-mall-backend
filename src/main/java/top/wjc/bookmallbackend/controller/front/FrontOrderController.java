package top.wjc.bookmallbackend.controller.front;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.wjc.bookmallbackend.common.PageResult;
import top.wjc.bookmallbackend.common.Result;
import top.wjc.bookmallbackend.dto.OrderCreateRequest;
import top.wjc.bookmallbackend.exception.UnauthorizedException;
import top.wjc.bookmallbackend.service.OrderService;
import top.wjc.bookmallbackend.vo.OrderCreateVO;
import top.wjc.bookmallbackend.vo.OrderDetailVO;
import top.wjc.bookmallbackend.vo.OrderListItemVO;

@Validated
@RestController
@RequestMapping("/api/front/orders")
public class FrontOrderController {

    private final OrderService orderService;

    public FrontOrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public Result<OrderCreateVO> create(@Valid @RequestBody OrderCreateRequest request, HttpServletRequest httpRequest) {
        Long userId = extractUserId(httpRequest);
        return Result.success(orderService.create(userId, request));
    }

    @PostMapping(value = "/{id}/pay", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> pay(@PathVariable @Min(value = 1, message = "订单ID必须大于0") Long id, HttpServletRequest httpRequest) {
        Long userId = extractUserId(httpRequest);
        String form = orderService.pay(userId, id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_HTML_VALUE + ";charset=UTF-8")
                .body(form);
    }

    @PostMapping("/{id}/cancel")
    public Result<Void> cancel(@PathVariable @Min(value = 1, message = "订单ID必须大于0") Long id, HttpServletRequest httpRequest) {
        Long userId = extractUserId(httpRequest);
        orderService.cancel(userId, id);
        return Result.success();
    }

    @GetMapping
    public Result<PageResult<OrderListItemVO>> list(@RequestParam(required = false) @Min(value = 1, message = "page必须大于0") Integer page,
                                                    @RequestParam(required = false) @Min(value = 1, message = "pageSize必须大于0") Integer pageSize,
                                                    HttpServletRequest httpRequest) {
        Long userId = extractUserId(httpRequest);
        return Result.success(orderService.list(userId, page, pageSize));
    }

    @GetMapping("/{id}")
    public Result<OrderDetailVO> detail(@PathVariable @Min(value = 1, message = "订单ID必须大于0") Long id, HttpServletRequest httpRequest) {
        Long userId = extractUserId(httpRequest);
        return Result.success(orderService.detail(userId, id));
    }

    private Long extractUserId(HttpServletRequest httpRequest) {
        Object userId = httpRequest.getAttribute("userId");
        if (userId == null) {
            throw new UnauthorizedException();
        }
        return Long.valueOf(userId.toString());
    }
}
