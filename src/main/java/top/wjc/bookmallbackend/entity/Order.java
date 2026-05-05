package top.wjc.bookmallbackend.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
/**
 * 订单主表实体。
 *
 * <p>对应数据库中的 `order` 表，保存订单号、金额、状态、地址、支付时间等主信息。
 */
public class Order {
    private Long id;
    private Integer version;
    private String orderNo;
    private String tradeNo;
    private Long userId;
    private BigDecimal totalAmount;
    private Integer status;
    private Long addressId;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private LocalDateTime payTime;
    private LocalDateTime shipTime;
}
