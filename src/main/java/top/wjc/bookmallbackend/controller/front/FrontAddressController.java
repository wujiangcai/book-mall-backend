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
import top.wjc.bookmallbackend.dto.AddressCreateRequest;
import top.wjc.bookmallbackend.dto.AddressUpdateRequest;
import top.wjc.bookmallbackend.service.AddressService;
import top.wjc.bookmallbackend.vo.AddressVO;

import java.util.List;

@RestController
@RequestMapping("/api/front/user/addresses")
public class FrontAddressController {

    private final AddressService addressService;

    public FrontAddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    @GetMapping
    public Result<List<AddressVO>> list(HttpServletRequest request) {
        Object userId = request.getAttribute("userId");
        return Result.success(addressService.list(Long.valueOf(userId.toString())));
    }

    @PostMapping
    public Result<Void> create(@Valid @RequestBody AddressCreateRequest request, HttpServletRequest httpRequest) {
        Object userId = httpRequest.getAttribute("userId");
        addressService.create(Long.valueOf(userId.toString()), request);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id,
                               @Valid @RequestBody AddressUpdateRequest request,
                               HttpServletRequest httpRequest) {
        Object userId = httpRequest.getAttribute("userId");
        addressService.update(Long.valueOf(userId.toString()), id, request);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id, HttpServletRequest httpRequest) {
        Object userId = httpRequest.getAttribute("userId");
        addressService.delete(Long.valueOf(userId.toString()), id);
        return Result.success();
    }

    @PutMapping("/{id}/default")
    public Result<Void> setDefault(@PathVariable Long id, HttpServletRequest httpRequest) {
        Object userId = httpRequest.getAttribute("userId");
        addressService.setDefault(Long.valueOf(userId.toString()), id);
        return Result.success();
    }
}
