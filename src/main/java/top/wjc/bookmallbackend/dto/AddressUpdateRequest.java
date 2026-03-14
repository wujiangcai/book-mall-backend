package top.wjc.bookmallbackend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class AddressUpdateRequest {

    @NotBlank(message = "收货人姓名不能为空")
    private String receiverName;

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^\\d{11}$", message = "手机号必须为11位数字")
    private String phone;

    private String province;

    private String city;

    private String district;

    @NotBlank(message = "详细地址不能为空")
    private String detailAddress;
}
