package top.wjc.bookmallbackend.exception;

public class InvalidOrderStatusException extends BusinessException {
    public InvalidOrderStatusException() {
        super(1002, "订单状态异常");
    }
}
