package top.wjc.bookmallbackend.constant;

public enum OrderStatus {
    UNPAID(0),
    PAID(1),
    PENDING_SHIP(2),
    SHIPPED(3),
    COMPLETED(4),
    CANCELLED(5);

    private final int code;

    OrderStatus(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
