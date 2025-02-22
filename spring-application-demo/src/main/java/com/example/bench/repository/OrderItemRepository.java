package com.example.bench.repository;

import com.example.bench.entity.OrderItem;
import com.example.bench.vo.ProductSalesVO;
import com.example.bench.vo.ShopSalesVO;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

@Tag(name = "订单项数据访问层", description = "订单项相关的数据库操作")
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    
    /**
     * 查询订单的所有商品项
     * @param orderId 订单ID
     * @return 订单商品项列表
     */
    @Query("SELECT oi FROM OrderItem oi WHERE oi.order.orderId = :orderId")
    List<OrderItem> findByOrderId(@Param("orderId") Long orderId);
    
    /**
     * 获取热销商品排行
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param pageable 分页参数
     * @return 商品销售统计列表
     */
    @Query("SELECT new com.example.bench.vo.ProductSalesVO(oi.spuId, oi.spuName, " +
           "SUM(oi.count), SUM(oi.spuTotalAmount)) " +
           "FROM OrderItem oi " +
           "WHERE (:startTime IS NULL OR oi.createTime >= :startTime) " +
           "AND (:endTime IS NULL OR oi.createTime <= :endTime) " +
           "GROUP BY oi.spuId, oi.spuName " +
           "ORDER BY SUM(oi.count) DESC")
    Page<ProductSalesVO> findTopSellingProducts(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            Pageable pageable);
    
    /**
     * 获取店铺销售统计
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param pageable 分页参数
     * @return 店铺销售统计列表
     */
    @Query("SELECT new com.example.bench.vo.ShopSalesVO(oi.shopId, " +
           "SUM(oi.spuTotalAmount), COUNT(DISTINCT oi.order.orderId)) " +
           "FROM OrderItem oi " +
           "WHERE (:startTime IS NULL OR oi.createTime >= :startTime) " +
           "AND (:endTime IS NULL OR oi.createTime <= :endTime) " +
           "GROUP BY oi.shopId " +
           "ORDER BY SUM(oi.spuTotalAmount) DESC")
    Page<ShopSalesVO> calculateShopSales(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            Pageable pageable);
}
