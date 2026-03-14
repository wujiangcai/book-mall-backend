package top.wjc.bookmallbackend.controller.front;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.wjc.bookmallbackend.common.Result;
import top.wjc.bookmallbackend.dto.CartAddRequest;
import top.wjc.bookmallbackend.dto.CartUpdateRequest;
import top.wjc.bookmallbackend.service.CartService;
import top.wjc.bookmallbackend.vo.CartItemVO;

import java.util.List;

@RestController
@RequestMapping("/api/front/cart")
public class FrontCartController {

    private final CartService cartService;

    public FrontCartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public Result<List<CartItemVO>> list(HttpServletRequest request) {
        Object userId = request.getAttribute("userId");
        return Result.success(cartService.list(Long.valueOf(userId.toString())));
    }

    @PostMapping
    public Result<Void> add(@Valid @RequestBody CartAddRequest request, HttpServletRequest httpRequest) {
        Object userId = httpRequest.getAttribute("userId");
        cartService.add(Long.valueOf(userId.toString()), request);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id,
                               @Valid @RequestBody CartUpdateRequest request,
                               HttpServletRequest httpRequest) {
        Object userId = httpRequest.getAttribute("userId");
        cartService.updateQuantity(Long.valueOf(userId.toString()), id, request);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id, HttpServletRequest httpRequest) {
        Object userId = httpRequest.getAttribute("userId");
        cartService.delete(Long.valueOf(userId.toString()), id);
        return Result.success();
    }
}
