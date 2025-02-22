package com.example.bench.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "商品销售统计数据")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductSalesVO {
    @Schema(description = "商品SPU ID")
    private Long spuId;
    
    @Schema(description = "商品名称")
    private String spuName;
    
    @Schema(description = "销售数量")
    private Long totalSold;
    
    @Schema(description = "销售总金额(分)")
    private Long totalAmount;
}
