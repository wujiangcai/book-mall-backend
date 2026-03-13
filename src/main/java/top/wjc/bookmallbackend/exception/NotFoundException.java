package top.wjc.bookmallbackend.exception;

public class NotFoundException extends BusinessException {
    public NotFoundException() {
        super(404, "资源不存在");
    }
}
