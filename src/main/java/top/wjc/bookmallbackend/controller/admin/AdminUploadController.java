package top.wjc.bookmallbackend.controller.admin;

import jakarta.validation.constraints.NotNull;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import top.wjc.bookmallbackend.common.Result;
import top.wjc.bookmallbackend.service.UploadService;

/**
 * 后台文件上传控制器。
 *
 * <p>上传入口只暴露给后台，用于上传轮播图和图书封面等图片资源。
 */
@Validated
@RestController
@RequestMapping("/api/admin/upload")
public class AdminUploadController {

    private final UploadService uploadService;

    public AdminUploadController(UploadService uploadService) {
        this.uploadService = uploadService;
    }

    @PostMapping(value = "/banner", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    /**
     * 上传轮播图图片。
     */
    public Result<String> uploadBanner(@NotNull @RequestPart("file") org.springframework.web.multipart.MultipartFile file) {
        return Result.success(uploadService.uploadBanner(file));
    }

    @PostMapping(value = "/book-cover", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    /**
     * 上传图书封面图片。
     */
    public Result<String> uploadBookCover(@NotNull @RequestPart("file") org.springframework.web.multipart.MultipartFile file) {
        return Result.success(uploadService.uploadBookCover(file));
    }
}
