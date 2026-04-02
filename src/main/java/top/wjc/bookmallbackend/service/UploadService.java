package top.wjc.bookmallbackend.service;

import org.springframework.web.multipart.MultipartFile;

public interface UploadService {
    String uploadBanner(MultipartFile file);

    String uploadBookCover(MultipartFile file);

    String resolveBannerUrl(String imageUrl);

    String resolveBookCoverUrl(String imageUrl);
}
