package top.wjc.bookmallbackend.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Address {
    private Long id;
    private Long userId;
    private String receiverName;
    private String phone;
    private String province;
    private String city;
    private String district;
    private String detailAddress;
    private Integer isDefault;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
