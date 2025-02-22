package com.example.bench.controller;

import com.example.bench.entity.Order;
import com.example.bench.entity.OrderItem;
import com.example.bench.service.OrderService;
import com.example.bench.vo.OrderStatVO;
import com.example.bench.vo.ProductSalesVO;
import com.example.bench.vo.ShopSalesVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Tag(name = "订单管理接口", description = "提供订单查询和统计功能")
@RestController
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @Operation(summary = "分页查询订单列表", description = "根据用户ID和订单状态分页查询订单列表")
    @GetMapping("/list")
    public Page<Order> getOrders(
            @Parameter(description = "页码，从1开始") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "用户ID") @RequestParam(required = false) Long userId,
            @Parameter(description = "订单状态") @RequestParam(required = false) Integer status) {
        return orderService.findOrders(userId, status, PageRequest.of(page - 1, size));
    }

    @Operation(summary = "查询订单详情", description = "获取订单的商品明细")
    @GetMapping("/{orderId}/items")
    public List<OrderItem> getOrderItems(
            @Parameter(description = "订单ID") @PathVariable Long orderId) {
        return orderService.getOrderItems(orderId);
    }

    @Operation(summary = "获取地区订单统计")
    @GetMapping("/stats/regions")
    public List<OrderStatVO> getRegionStats(
            @Parameter(description = "开始时间") 
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @Parameter(description = "结束时间") 
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        return orderService.getRegionStats(startTime, endTime);
    }

    @Operation(summary = "获取每小时订单统计")
    @GetMapping("/stats/hourly")
    public Map<Integer, Long> getHourlyStats(
            @Parameter(description = "开始时间") 
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @Parameter(description = "结束时间") 
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        return orderService.getHourlyStats(startTime, endTime);
    }

    @Operation(summary = "分页获取商品销量排行", description = "获取销量最高的商品列表")
    @GetMapping("/stats/products")
    public Page<ProductSalesVO> getProductSales(
            @Parameter(description = "页码，从1开始") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "开始时间") 
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @Parameter(description = "结束时间") 
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        return orderService.getProductSales(startTime, endTime, PageRequest.of(page - 1, size));
    }

    @Operation(summary = "分页获取店铺销售统计", description = "统计指定时间范围内各店铺的销售情况")
    @GetMapping("/stats/shops")
    public Page<ShopSalesVO> getShopSales(
            @Parameter(description = "页码，从1开始") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "开始时间") 
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @Parameter(description = "结束时间") 
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        return orderService.getShopSales(startTime, endTime, PageRequest.of(page - 1, size));
    }
}
