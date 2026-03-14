package top.wjc.bookmallbackend.service;

import top.wjc.bookmallbackend.common.PageResult;
import top.wjc.bookmallbackend.dto.OrderCreateRequest;
import top.wjc.bookmallbackend.vo.AdminOrderDetailVO;
import top.wjc.bookmallbackend.vo.AdminOrderListItemVO;
import top.wjc.bookmallbackend.vo.OrderCreateVO;
import top.wjc.bookmallbackend.vo.OrderDetailVO;
import top.wjc.bookmallbackend.vo.OrderListItemVO;

public interface OrderService {
    OrderCreateVO create(Long userId, OrderCreateRequest request);

    void pay(Long userId, Long orderId);

    void cancel(Long userId, Long orderId);

    PageResult<OrderListItemVO> list(Long userId, Integer page, Integer pageSize);

    OrderDetailVO detail(Long userId, Long orderId);

    PageResult<AdminOrderListItemVO> listAdmin(Integer page, Integer pageSize, Integer status, String orderNo, Long userId);

    AdminOrderDetailVO detailAdmin(Long orderId);

    void ship(Long orderId);
}
