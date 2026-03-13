package top.wjc.bookmallbackend.exception;

public class InsufficientStockException extends BusinessException {
    public InsufficientStockException() {
        super(1001, "库存不足");
    }
}
