package top.wjc.bookmallbackend.exception;

public class ForbiddenException extends BusinessException {
    public ForbiddenException() {
        super(403, "禁止访问");
    }
}
