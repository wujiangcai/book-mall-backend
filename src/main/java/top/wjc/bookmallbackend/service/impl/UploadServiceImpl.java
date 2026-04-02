package top.wjc.bookmallbackend.service.impl;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.http.HttpMethodName;
import com.qcloud.cos.model.GeneratePresignedUrlRequest;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.ResponseHeaderOverrides;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import top.wjc.bookmallbackend.exception.BusinessException;
import top.wjc.bookmallbackend.service.UploadService;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Date;
import java.util.UUID;

@Service
public class UploadServiceImpl implements UploadService {

    private static final long MAX_SIZE = 2 * 1024 * 1024;

    private final COSClient cosClient;

    @Value("${cos.bucket:}")
    private String bucket;

    @Value("${cos.base-url:}")
    private String baseUrl;

    @Value("${cos.sign-duration-seconds:604800}")
    private long signDurationSeconds;

    public UploadServiceImpl(ObjectProvider<COSClient> cosClientProvider) {
        this.cosClient = cosClientProvider.getIfAvailable();
    }

    @Override
    public String uploadBanner(MultipartFile file) {
        return uploadImage(file, "banners/");
    }

    @Override
    public String uploadBookCover(MultipartFile file) {
        return uploadImage(file, "books/");
    }

    @Override
    public String resolveBannerUrl(String imageUrl) {
        return resolveImageUrl(imageUrl);
    }

    @Override
    public String resolveBookCoverUrl(String imageUrl) {
        return resolveImageUrl(imageUrl);
    }

    private String uploadImage(MultipartFile file, String directory) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(400, "文件不能为空");
        }
        if (file.getSize() > MAX_SIZE) {
            throw new BusinessException(400, "文件大小不能超过2MB");
        }
        String contentType = file.getContentType();
        if (contentType == null || !isImageType(contentType)) {
            throw new BusinessException(400, "仅支持jpg/jpeg/png/webp图片");
        }
        if (cosClient == null || bucket == null || bucket.isBlank() || baseUrl == null || baseUrl.isBlank()) {
            throw new BusinessException(500, "COS未配置");
        }
        String key = buildKey(directory, file.getOriginalFilename());
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.getSize());
        metadata.setContentType(contentType);
        try (InputStream inputStream = file.getInputStream()) {
            PutObjectRequest request = new PutObjectRequest(bucket, key, inputStream, metadata);
            cosClient.putObject(request);
        } catch (IOException ex) {
            throw new BusinessException(500, "上传失败");
        }
        return buildAccessibleUrl(key);
    }

    private String resolveImageUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank() || cosClient == null || bucket == null || bucket.isBlank()) {
            return imageUrl;
        }
        String key = extractKey(imageUrl);
        if (key == null || key.isBlank()) {
            return imageUrl;
        }
        return buildAccessibleUrl(key);
    }

    private String buildKey(String directory, String originalFilename) {
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf('.'));
        }
        return directory + UUID.randomUUID().toString().replace("-", "") + extension;
    }

    private boolean isImageType(String contentType) {
        return contentType.equals("image/jpeg")
                || contentType.equals("image/jpg")
                || contentType.equals("image/png")
                || contentType.equals("image/webp");
    }

    private String buildAccessibleUrl(String key) {
        String normalized = normalizeUrl(baseUrl, key);
        if (cosClient == null || signDurationSeconds <= 0) {
            return normalized;
        }
        Date expiration = new Date(System.currentTimeMillis() + signDurationSeconds * 1000);
        GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucket, key, HttpMethodName.GET);
        request.setExpiration(expiration);
        ResponseHeaderOverrides overrides = new ResponseHeaderOverrides();
        overrides.setContentDisposition("inline");
        request.setResponseHeaders(overrides);
        return cosClient.generatePresignedUrl(request).toString();
    }

    private String extractKey(String imageUrl) {
        try {
            URI uri = URI.create(imageUrl);
            String path = uri.getPath();
            if (path == null || path.isBlank() || "/".equals(path)) {
                return null;
            }
            return path.startsWith("/") ? path.substring(1) : path;
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private String normalizeUrl(String base, String key) {
        if (base.endsWith("/")) {
            return base + key;
        }
        return base + "/" + key;
    }
}
