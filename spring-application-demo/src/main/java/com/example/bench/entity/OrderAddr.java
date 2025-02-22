package com.example.bench.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "订单收货地址信息")
@Getter
@Setter
@Entity
@Table(name = "order_addr")
public class OrderAddr {
    @Schema(description = "订单收货地址ID")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_addr_id")
    private Long orderAddrId;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "收货人姓名")
    private String consignee;

    @Schema(description = "省份ID")
    private Long provinceId;

    @Schema(description = "省份")
    private String province;

    @Schema(description = "城市ID")
    private Long cityId;

    @Schema(description = "城市")
    private String city;

    @Schema(description = "区域ID")
    private Long areaId;

    @Schema(description = "区域")
    private String area;

    @Schema(description = "详细地址")
    private String addr;

    @Schema(description = "邮编")
    private String postCode;

    @Schema(description = "手机号码")
    private String mobile;

    @Schema(description = "经度")
    private BigDecimal lng;

    @Schema(description = "纬度")
    private BigDecimal lat;
}
