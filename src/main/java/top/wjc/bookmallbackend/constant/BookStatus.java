package top.wjc.bookmallbackend.constant;

public enum BookStatus {
    OFF_SHELF(0),
    ON_SHELF(1);

    private final int code;

    BookStatus(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
