package top.wjc.bookmallbackend.exception;

public class InvalidStatusException extends BusinessException {
    public InvalidStatusException(String message) {
        super(400, message);
    }
}
