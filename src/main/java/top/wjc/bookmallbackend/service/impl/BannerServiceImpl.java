package top.wjc.bookmallbackend.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.wjc.bookmallbackend.common.PageResult;
import top.wjc.bookmallbackend.constant.CommonStatus;
import top.wjc.bookmallbackend.dto.BannerCreateRequest;
import top.wjc.bookmallbackend.dto.BannerSortRequest;
import top.wjc.bookmallbackend.dto.BannerUpdateRequest;
import top.wjc.bookmallbackend.entity.Banner;
import top.wjc.bookmallbackend.exception.InvalidStatusException;
import top.wjc.bookmallbackend.exception.NotFoundException;
import top.wjc.bookmallbackend.mapper.BannerMapper;
import top.wjc.bookmallbackend.service.BannerService;
import top.wjc.bookmallbackend.service.UploadService;
import top.wjc.bookmallbackend.vo.BannerVO;

import java.util.List;
import java.util.stream.Collectors;

@Service
/**
 * 轮播图服务实现。
 *
 * <p>后台负责维护轮播图内容，前台只读取已启用的轮播图并按排序展示。
 */
public class BannerServiceImpl implements BannerService {

    private final BannerMapper bannerMapper;
    private final UploadService uploadService;

    public BannerServiceImpl(BannerMapper bannerMapper, UploadService uploadService) {
        this.bannerMapper = bannerMapper;
        this.uploadService = uploadService;
    }

    @Override
    /**
     * 后台分页查询轮播图。
     */
    public PageResult<BannerVO> listAdmin(Integer page, Integer pageSize) {
        int currentPage = normalizePage(page);
        int size = normalizeSize(pageSize);
        int offset = (currentPage - 1) * size;
        long total = bannerMapper.countAdminList();
        List<BannerVO> list = toAdminVOs(bannerMapper.selectAdminListPaged(offset, size));
        return new PageResult<>(total, list, currentPage, size);
    }

    @Override
    public List<BannerVO> listAdmin() {
        return toAdminVOs(bannerMapper.selectAdminList());
    }

    @Override
    @Transactional
    /**
     * 新增轮播图。
     */
    public void create(BannerCreateRequest request) {
        validateStatus(request.getStatus());
        Banner banner = Banner.builder()
                .imageUrl(uploadService.resolveBannerUrl(request.getImageUrl()))
                .linkUrl(request.getLinkUrl())
                .sortOrder(request.getSortOrder() == null ? 0 : request.getSortOrder())
                .status(request.getStatus())
                .build();
        bannerMapper.insert(banner);
    }

    @Override
    @Transactional
    /**
     * 修改轮播图。
     */
    public void update(Long id, BannerUpdateRequest request) {
        Banner existing = bannerMapper.selectById(id);
        if (existing == null || isSoftDeleted(existing.getStatus())) {
            throw new NotFoundException();
        }
        validateStatus(request.getStatus());
        Banner banner = Banner.builder()
                .id(id)
                .imageUrl(uploadService.resolveBannerUrl(request.getImageUrl()))
                .linkUrl(request.getLinkUrl())
                .sortOrder(request.getSortOrder() == null ? existing.getSortOrder() : request.getSortOrder())
                .status(request.getStatus())
                .build();
        bannerMapper.update(banner);
    }

    @Override
    @Transactional
    /**
     * 删除轮播图。
     */
    public void delete(Long id) {
        Banner existing = bannerMapper.selectById(id);
        if (existing == null || isSoftDeleted(existing.getStatus())) {
            throw new NotFoundException();
        }
        bannerMapper.softDelete(id);
    }

    @Override
    @Transactional
    /**
     * 修改轮播图排序。
     */
    public void updateSort(Long id, BannerSortRequest request) {
        Banner existing = bannerMapper.selectById(id);
        if (existing == null || isSoftDeleted(existing.getStatus())) {
            throw new NotFoundException();
        }
        bannerMapper.updateSort(id, request.getSortOrder());
    }

    @Override
    /**
     * 查询前台轮播图列表。
     */
    public List<BannerVO> listFront() {
        return bannerMapper.selectFrontList().stream()
                .map(banner -> new BannerVO(
                        banner.getId(),
                        uploadService.resolveBannerUrl(banner.getImageUrl()),
                        banner.getLinkUrl(),
                        banner.getSortOrder(),
                        banner.getStatus(),
                        banner.getCreateTime()
                ))
                .collect(Collectors.toList());
    }

    private int normalizePage(Integer page) {
        return page == null || page < 1 ? 1 : page;
    }

    private int normalizeSize(Integer pageSize) {
        int size = pageSize == null ? 20 : pageSize;
        if (size < 1) {
            return 20;
        }
        return Math.min(size, 100);
    }

    private List<BannerVO> toAdminVOs(List<Banner> banners) {
        return banners.stream()
                .map(banner -> new BannerVO(
                        banner.getId(),
                        uploadService.resolveBannerUrl(banner.getImageUrl()),
                        banner.getLinkUrl(),
                        banner.getSortOrder(),
                        banner.getStatus(),
                        banner.getCreateTime()
                ))
                .collect(Collectors.toList());
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
