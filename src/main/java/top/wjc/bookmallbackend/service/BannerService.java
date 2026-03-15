package top.wjc.bookmallbackend.service;

import top.wjc.bookmallbackend.common.PageResult;
import top.wjc.bookmallbackend.dto.BannerCreateRequest;
import top.wjc.bookmallbackend.dto.BannerSortRequest;
import top.wjc.bookmallbackend.dto.BannerUpdateRequest;
import top.wjc.bookmallbackend.vo.BannerVO;

import java.util.List;

public interface BannerService {

    PageResult<BannerVO> listAdmin(Integer page, Integer pageSize);

    List<BannerVO> listAdmin();

    void create(BannerCreateRequest request);

    void update(Long id, BannerUpdateRequest request);

    void delete(Long id);

    void updateSort(Long id, BannerSortRequest request);

    List<BannerVO> listFront();
}
