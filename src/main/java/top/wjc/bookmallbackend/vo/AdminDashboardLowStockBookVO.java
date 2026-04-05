package top.wjc.bookmallbackend.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardLowStockBookVO {
    private Long id;
    private String bookName;
    private String author;
    private Integer stock;
    private Integer status;
}
