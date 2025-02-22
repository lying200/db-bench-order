package com.example.bench.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "店铺销售统计数据")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShopSalesVO {
    @Schema(description = "店铺ID")
    private Long shopId;

    @Schema(description = "销售总金额(分)")
    private Long totalAmount;

    @Schema(description = "订单总数")
    private Long orderCount;
}
