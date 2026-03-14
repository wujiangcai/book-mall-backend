package top.wjc.bookmallbackend.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AddressVO {
    private Long id;
    private String receiverName;
    private String phone;
    private String province;
    private String city;
    private String district;
    private String detailAddress;
    private Integer isDefault;
}
