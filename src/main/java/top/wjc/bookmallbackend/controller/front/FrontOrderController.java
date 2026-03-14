package top.wjc.bookmallbackend.controller.front;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
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

    @PostMapping("/{id}/pay")
    public Result<Void> pay(@PathVariable Long id, HttpServletRequest httpRequest) {
        Object userId = httpRequest.getAttribute("userId");
        orderService.pay(Long.valueOf(userId.toString()), id);
        return Result.success();
    }

    @PostMapping("/{id}/cancel")
    public Result<Void> cancel(@PathVariable Long id, HttpServletRequest httpRequest) {
        Object userId = httpRequest.getAttribute("userId");
        orderService.cancel(Long.valueOf(userId.toString()), id);
        return Result.success();
    }

    @GetMapping
    public Result<PageResult<OrderListItemVO>> list(@RequestParam(required = false) Integer page,
                                                    @RequestParam(required = false) Integer pageSize,
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
