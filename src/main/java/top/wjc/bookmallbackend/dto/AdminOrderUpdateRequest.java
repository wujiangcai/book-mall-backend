package top.wjc.bookmallbackend.dto;

import lombok.Data;

@Data
public class AdminOrderUpdateRequest {

    private Integer status;

    private Long addressId;
}
