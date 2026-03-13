package top.wjc.bookmallbackend.exception;

public class BookOffShelfException extends BusinessException {
    public BookOffShelfException() {
        super(1003, "图书已下架");
    }
}
