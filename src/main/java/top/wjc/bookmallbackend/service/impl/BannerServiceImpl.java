package top.wjc.bookmallbackend.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.wjc.bookmallbackend.constant.CommonStatus;
import top.wjc.bookmallbackend.dto.BannerCreateRequest;
import top.wjc.bookmallbackend.dto.BannerSortRequest;
import top.wjc.bookmallbackend.dto.BannerUpdateRequest;
import top.wjc.bookmallbackend.entity.Banner;
import top.wjc.bookmallbackend.exception.InvalidStatusException;
import top.wjc.bookmallbackend.exception.NotFoundException;
import top.wjc.bookmallbackend.mapper.BannerMapper;
import top.wjc.bookmallbackend.service.BannerService;
import top.wjc.bookmallbackend.vo.BannerVO;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BannerServiceImpl implements BannerService {

    private final BannerMapper bannerMapper;

    public BannerServiceImpl(BannerMapper bannerMapper) {
        this.bannerMapper = bannerMapper;
    }

    @Override
    public List<BannerVO> listAdmin() {
        return bannerMapper.selectAdminList().stream()
                .map(banner -> new BannerVO(
                        banner.getId(),
                        banner.getImageUrl(),
                        banner.getLinkUrl(),
                        banner.getSortOrder(),
                        banner.getStatus(),
                        banner.getCreateTime()
                ))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void create(BannerCreateRequest request) {
        validateStatus(request.getStatus());
        Banner banner = Banner.builder()
                .imageUrl(request.getImageUrl())
                .linkUrl(request.getLinkUrl())
                .sortOrder(request.getSortOrder() == null ? 0 : request.getSortOrder())
                .status(request.getStatus())
                .build();
        bannerMapper.insert(banner);
    }

    @Override
    @Transactional
    public void update(Long id, BannerUpdateRequest request) {
        Banner existing = bannerMapper.selectById(id);
        if (existing == null || isSoftDeleted(existing.getStatus())) {
            throw new NotFoundException();
        }
        validateStatus(request.getStatus());
        Banner banner = Banner.builder()
                .id(id)
                .imageUrl(request.getImageUrl())
                .linkUrl(request.getLinkUrl())
                .sortOrder(request.getSortOrder() == null ? existing.getSortOrder() : request.getSortOrder())
                .status(request.getStatus())
                .build();
        bannerMapper.update(banner);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Banner existing = bannerMapper.selectById(id);
        if (existing == null || isSoftDeleted(existing.getStatus())) {
            throw new NotFoundException();
        }
        bannerMapper.softDelete(id);
    }

    @Override
    @Transactional
    public void updateSort(Long id, BannerSortRequest request) {
        Banner existing = bannerMapper.selectById(id);
        if (existing == null || isSoftDeleted(existing.getStatus())) {
            throw new NotFoundException();
        }
        bannerMapper.updateSort(id, request.getSortOrder());
    }

    @Override
    public List<BannerVO> listFront() {
        return bannerMapper.selectFrontList();
    }

    private void validateStatus(Integer status) {
        if (status == null) {
            throw new InvalidStatusException("状态不能为空");
        }
        if (status != CommonStatus.ENABLED.getCode() && status != CommonStatus.DISABLED.getCode()) {
            throw new InvalidStatusException("轮播图状态不合法");
        }
    }

    private boolean isSoftDeleted(Integer status) {
        return status != null && status == -1;
    }
}
