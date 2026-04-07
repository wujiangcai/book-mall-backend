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
        Object userId = httpRequest.getAttribute("userId");
        return Result.success(orderService.create(Long.valueOf(userId.toString()), request));
    }

    @PostMapping(value = "/{id}/pay", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> pay(@PathVariable Long id, HttpServletRequest httpRequest) {
        Object userId = httpRequest.getAttribute("userId");
        String form = orderService.pay(Long.valueOf(userId.toString()), id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_HTML_VALUE + ";charset=UTF-8")
                .body(form);
    }

    @PostMapping("/{id}/cancel")
    public Result<Void> cancel(@PathVariable Long id, HttpServletRequest httpRequest) {
        Object userId = httpRequest.getAttribute("userId");
        orderService.cancel(Long.valueOf(userId.toString()), id);
        return Result.success();
    }

    @GetMapping
    public Result<PageResult<OrderListItemVO>> list(@RequestParam(required = false) @Min(value = 1, message = "page必须大于0") Integer page,
                                                    @RequestParam(required = false) @Min(value = 1, message = "pageSize必须大于0") Integer pageSize,
                                                    HttpServletRequest httpRequest) {
        Object userId = httpRequest.getAttribute("userId");
        return Result.success(orderService.list(Long.valueOf(userId.toString()), page, pageSize));
    }

    @GetMapping("/{id}")
    public Result<OrderDetailVO> detail(@PathVariable Long id, HttpServletRequest httpRequest) {
        Object userId = httpRequest.getAttribute("userId");
        return Result.success(orderService.detail(Long.valueOf(userId.toString()), id));
    }
}
