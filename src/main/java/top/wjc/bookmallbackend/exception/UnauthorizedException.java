package top.wjc.bookmallbackend.exception;

public class UnauthorizedException extends BusinessException {
    public UnauthorizedException() {
        super(401, "未授权");
    }
}
