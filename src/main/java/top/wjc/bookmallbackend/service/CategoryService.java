package top.wjc.bookmallbackend.service;

import top.wjc.bookmallbackend.dto.CategoryCreateRequest;
import top.wjc.bookmallbackend.dto.CategoryStatusRequest;
import top.wjc.bookmallbackend.dto.CategoryUpdateRequest;
import top.wjc.bookmallbackend.vo.CategoryAdminVO;
import top.wjc.bookmallbackend.vo.CategoryTreeVO;

import java.util.List;

public interface CategoryService {
    List<CategoryTreeVO> getFrontTree();

    List<CategoryAdminVO> listAdmin();

    void create(CategoryCreateRequest request);

    void update(Long id, CategoryUpdateRequest request);

    void delete(Long id);

    void updateStatus(Long id, CategoryStatusRequest request);
}
