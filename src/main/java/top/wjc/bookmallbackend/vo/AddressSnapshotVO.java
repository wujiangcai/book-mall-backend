package top.wjc.bookmallbackend.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
/**
 * 订单中的地址快照对象。
 *
 * <p>用于把下单时需要展示的地址信息组装成紧凑结构返回前端。
 */
public class AddressSnapshotVO {
    private String receiverName;
    private String phone;
    private String fullAddress;
}
