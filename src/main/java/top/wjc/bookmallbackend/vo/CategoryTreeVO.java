package top.wjc.bookmallbackend.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class CategoryTreeVO {
    private Long id;
    private String categoryName;
    private List<CategoryTreeVO> children;
}
