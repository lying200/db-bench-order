package com.example.bench.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Schema(description = "订单项信息")
@Getter
@Setter
@Entity
@Table(name = "order_item")
public class OrderItem {
    @Schema(description = "订单项ID")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_item_id")
    private Long orderItemId;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "店铺ID")
    private Long shopId;

    @Schema(description = "商品分类ID")
    private Long categoryId;

    @Schema(description = "商品SPU ID")
    private Long spuId;

    @Schema(description = "商品SKU ID")
    private Long skuId;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "购买数量")
    private Integer count;

    @Schema(description = "商品名称")
    private String spuName;

    @Schema(description = "SKU名称")
    private String skuName;

    @Schema(description = "商品主图")
    private String pic;

    @Schema(description = "配送类型：3-无需快递")
    private Integer deliveryType;

    @Schema(description = "加入购物车时间")
    private LocalDateTime shopCartTime;

    @Schema(description = "商品单价(分)")
    private Long price;

    @Schema(description = "商品总金额(分)")
    private Long spuTotalAmount;

    @Schema(description = "所属订单信息")
    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;
}
