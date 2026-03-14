package top.wjc.bookmallbackend.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AddressSnapshotVO {
    private String receiverName;
    private String phone;
    private String fullAddress;
}
