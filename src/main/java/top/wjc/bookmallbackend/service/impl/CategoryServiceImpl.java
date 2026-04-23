package top.wjc.bookmallbackend.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.wjc.bookmallbackend.common.PageResult;
import top.wjc.bookmallbackend.constant.CommonStatus;
import top.wjc.bookmallbackend.dto.CategoryCreateRequest;
import top.wjc.bookmallbackend.dto.CategoryStatusRequest;
import top.wjc.bookmallbackend.dto.CategoryUpdateRequest;
import top.wjc.bookmallbackend.entity.Category;
import top.wjc.bookmallbackend.exception.BusinessException;
import top.wjc.bookmallbackend.exception.InvalidStatusException;
import top.wjc.bookmallbackend.exception.NotFoundException;
import top.wjc.bookmallbackend.mapper.BookMapper;
import top.wjc.bookmallbackend.mapper.CategoryMapper;
import top.wjc.bookmallbackend.service.CategoryService;
import top.wjc.bookmallbackend.vo.CategoryAdminVO;
import top.wjc.bookmallbackend.vo.CategoryTreeVO;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryMapper categoryMapper;
    private final BookMapper bookMapper;

    public CategoryServiceImpl(CategoryMapper categoryMapper, BookMapper bookMapper) {
        this.categoryMapper = categoryMapper;
        this.bookMapper = bookMapper;
    }

    @Override
    public List<CategoryTreeVO> getFrontTree() {
        List<Category> categories = categoryMapper.selectAllEnabled();
        return buildTree(categories);
    }

    @Override
    public PageResult<CategoryAdminVO> listAdmin(Integer page, Integer pageSize) {
        int currentPage = normalizePage(page);
        int size = normalizeSize(pageSize);
        int offset = (currentPage - 1) * size;
        long total = categoryMapper.countAdminList();
        List<CategoryAdminVO> list = toAdminVOs(categoryMapper.selectAdminListPaged(offset, size));
        return new PageResult<>(total, list, currentPage, size);
    }

    @Override
    public List<CategoryAdminVO> listAdmin() {
        return toAdminVOs(categoryMapper.selectAdminList());
    }

    @Override
    @Transactional
    public void create(CategoryCreateRequest request) {
        if (categoryMapper.countByName(request.getCategoryName()) > 0) {
            throw new BusinessException(400, "分类名称已存在");
        }
        validateParent(request.getParentId());
        Category category = Category.builder()
                .categoryName(request.getCategoryName())
                .parentId(request.getParentId())
                .sortOrder(request.getSortOrder())
                .status(CommonStatus.ENABLED.getCode())
                .build();
        categoryMapper.insert(category);
    }

    @Override
    @Transactional
    public void update(Long id, CategoryUpdateRequest request) {
        Category existing = categoryMapper.selectById(id);
        if (existing == null || isSoftDeleted(existing.getStatus())) {
            throw new NotFoundException();
        }
        if (categoryMapper.countByNameExcludeId(request.getCategoryName(), id) > 0) {
            throw new BusinessException(400, "分类名称已存在");
        }
        validateParent(request.getParentId());
        Category category = Category.builder()
                .id(id)
                .categoryName(request.getCategoryName())
                .parentId(request.getParentId())
                .sortOrder(request.getSortOrder())
                .status(existing.getStatus())
                .build();
        categoryMapper.update(category);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Category existing = categoryMapper.selectById(id);
        if (existing == null || isSoftDeleted(existing.getStatus())) {
            throw new NotFoundException();
        }
        if (bookMapper.countByCategoryId(id) > 0) {
            throw new BusinessException(400, "分类下存在图书，无法删除");
        }
        categoryMapper.softDelete(id);
    }

    @Override
    @Transactional
    public void updateStatus(Long id, CategoryStatusRequest request) {
        Category existing = categoryMapper.selectById(id);
        if (existing == null || isSoftDeleted(existing.getStatus())) {
            throw new NotFoundException();
        }
        validateCategoryStatus(request.getStatus());
        categoryMapper.updateStatus(id, request.getStatus());
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

    private List<CategoryAdminVO> toAdminVOs(List<Category> categories) {
        Map<Long, String> categoryNameMap = categoryMapper.selectAdminList().stream()
                .collect(Collectors.toMap(Category::getId, Category::getCategoryName));
        return categories.stream()
                .map(category -> new CategoryAdminVO(
                        category.getId(),
                        category.getCategoryName(),
                        category.getParentId(),
                        category.getParentId() == null || category.getParentId() == 0
                                ? null
                                : categoryNameMap.get(category.getParentId()),
                        category.getSortOrder(),
                        category.getStatus(),
                        category.getCreateTime()
                ))
                .collect(Collectors.toList());
    }

    private void validateParent(Long parentId) {
        if (parentId == null || parentId == 0) {
            return;
        }
        Category parent = categoryMapper.selectById(parentId);
        if (parent == null || isSoftDeleted(parent.getStatus()) || isDisabled(parent.getStatus())) {
            throw new BusinessException(400, "父分类不可用");
        }
    }

    private boolean isSoftDeleted(Integer status) {
        return status != null && status == -1;
    }

    private boolean isDisabled(Integer status) {
        return status != null && status.equals(CommonStatus.DISABLED.getCode());
    }

    private void validateCategoryStatus(Integer status) {
        if (status == null) {
            throw new InvalidStatusException("状态不能为空");
        }
        if (status != CommonStatus.ENABLED.getCode() && status != CommonStatus.DISABLED.getCode()) {
            throw new InvalidStatusException("分类状态不合法");
        }
    }

    private List<CategoryTreeVO> buildTree(List<Category> categories) {
        Map<Long, List<Category>> grouped = categories.stream()
                .collect(Collectors.groupingBy(category -> category.getParentId() == null ? 0L : category.getParentId()));
        List<CategoryTreeVO> roots = new ArrayList<>();
        List<Category> rootCategories = grouped.getOrDefault(0L, new ArrayList<>());
        for (Category root : rootCategories) {
            roots.add(buildNode(root, grouped));
        }
        return roots;
    }

    private CategoryTreeVO buildNode(Category category, Map<Long, List<Category>> grouped) {
        List<CategoryTreeVO> children = new ArrayList<>();
        List<Category> childCategories = grouped.getOrDefault(category.getId(), new ArrayList<>());
        for (Category child : childCategories) {
            children.add(buildNode(child, grouped));
        }
        return new CategoryTreeVO(category.getId(), category.getCategoryName(), children);
    }
}
