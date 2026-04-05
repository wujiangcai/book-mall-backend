package top.wjc.bookmallbackend.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardHotBookVO {
    private Long bookId;
    private String bookName;
    private String author;
    private String coverImage;
    private Long salesCount;
    private BigDecimal salesAmount;
}
