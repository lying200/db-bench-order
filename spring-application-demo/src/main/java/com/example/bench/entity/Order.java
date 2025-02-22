package com.example.bench.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 订单信息
 */
@Schema(description = "订单信息")
@Getter
@Setter
@Entity
@Table(name = "`order`")
public class Order {
    /**
     * 订单ID
     */
    @Schema(description = "订单ID")
    @Id
    @Column(name = "order_id")
    private Long orderId;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    /**
     * 店铺ID
     */
    @Schema(description = "店铺ID")
    private Long shopId;

    /**
     * 用户ID
     */
    @Schema(description = "用户ID")
    private Long userId;

    /**
     * 配送类型：无需快递
     */
    @Schema(description = "配送类型：无需快递")
    private Integer deliveryType;

    /**
     * 店铺名称
     */
    @Schema(description = "店铺名称")
    private String shopName;

    /**
     * 订单总金额(分)
     */
    @Schema(description = "订单总金额(分)")
    private Long total;

    /**
     * 订单状态：1:待付款 2:待发货 3:待收货 5:成功 6:失败
     */
    @Schema(description = "订单状态：1:待付款 2:待发货 3:待收货 5:成功 6:失败")
    private Integer status;

    /**
     * 订单商品总数
     */
    @Schema(description = "订单商品总数")
    private Integer allCount;

    /**
     * 付款时间
     */
    @Schema(description = "付款时间")
    private LocalDateTime payTime;

    /**
     * 发货时间
     */
    @Schema(description = "发货时间")
    private LocalDateTime deliveryTime;

    /**
     * 完成时间
     */
    @Schema(description = "完成时间")
    private LocalDateTime finallyTime;

    /**
     * 结算时间
     */
    @Schema(description = "结算时间")
    private LocalDateTime settledTime;

    /**
     * 取消时间
     */
    @Schema(description = "取消时间")
    private LocalDateTime cancelTime;

    /**
     * 是否已支付，1:已支付 0:未支付
     */
    @Schema(description = "是否已支付，1:已支付 0:未支付")
    private Boolean isPayed;

    /**
     * 订单关闭原因：1-超时未支付 4-买家取消 15-已通过货到付款交易
     */
    @Schema(description = "订单关闭原因：1-超时未支付 4-买家取消 15-已通过货到付款交易")
    private Integer closeType;

    /**
     * 用户订单删除状态，0:未删除 1:回收站 2:永久删除
     */
    @Schema(description = "用户订单删除状态，0:未删除 1:回收站 2:永久删除")
    private Integer deleteStatus;

    /**
     * 订单版本号，每处理一次订单加1
     */
    @Schema(description = "订单版本号，每处理一次订单加1")
    private Integer version;

    /**
     * 订单地址信息
     */
    @Schema(description = "订单地址信息")
    @OneToOne
    @JoinColumn(name = "order_addr_id")
    private OrderAddr orderAddr;
}
