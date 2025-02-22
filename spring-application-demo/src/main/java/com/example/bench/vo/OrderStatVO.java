package com.example.bench.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "订单统计数据")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatVO {
    @Schema(description = "省份")
    private String province;

    @Schema(description = "订单数量")
    private Long orderCount;
}
