package top.wjc.bookmallbackend.controller.admin;

import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.wjc.bookmallbackend.common.PageResult;
import top.wjc.bookmallbackend.common.Result;
import top.wjc.bookmallbackend.dto.AdminOrderCreateRequest;
import top.wjc.bookmallbackend.dto.AdminOrderUpdateRequest;
import top.wjc.bookmallbackend.service.OrderService;
import top.wjc.bookmallbackend.vo.AdminOrderDetailVO;
import top.wjc.bookmallbackend.vo.AdminOrderListItemVO;

@Validated
@RestController
@RequestMapping("/api/admin/orders")
public class AdminOrderController {

    private final OrderService orderService;

    public AdminOrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public Result<PageResult<AdminOrderListItemVO>> list(@RequestParam(required = false) @Min(value = 1, message = "page必须大于0") Integer page,
                                                         @RequestParam(required = false) @Min(value = 1, message = "pageSize必须大于0") Integer pageSize,
                                                         @RequestParam(required = false) Integer status,
                                                         @RequestParam(required = false) String orderNo,
                                                         @RequestParam(required = false) @Min(value = 1, message = "userId必须大于0") Long userId) {
        return Result.success(orderService.listAdmin(page, pageSize, status, orderNo, userId));
    }

    @GetMapping("/{id}")
    public Result<AdminOrderDetailVO> detail(@PathVariable Long id) {
        return Result.success(orderService.detailAdmin(id));
    }

    @PostMapping
    public Result<Void> create(@Valid @RequestBody AdminOrderCreateRequest request) {
        orderService.createAdmin(request);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody AdminOrderUpdateRequest request) {
        orderService.updateAdmin(id, request);
        return Result.success();
    }

    @PutMapping("/{id}/cancel")
    public Result<Void> cancel(@PathVariable Long id) {
        orderService.cancelAdmin(id);
        return Result.success();
    }

    @PutMapping("/{id}/ship")
    public Result<Void> ship(@PathVariable Long id) {
        orderService.ship(id);
        return Result.success();
    }
}
