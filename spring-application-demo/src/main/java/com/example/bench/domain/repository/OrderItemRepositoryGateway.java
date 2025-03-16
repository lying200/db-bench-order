package com.example.bench.domain.repository;

import com.example.bench.entity.OrderItem;
import com.example.bench.vo.ProductSalesVO;
import com.example.bench.vo.ShopSalesVO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Gateway interface for OrderItem repository implementations
 * This follows the Repository pattern from DDD
 */
public interface OrderItemRepositoryGateway {

    /**
     * Find order items by order ID
     * @param orderId Order ID
     * @return List of order items
     */
    List<OrderItem> findByOrderId(Long orderId);

    /**
     * Get top selling products within a time range
     * @param startTime Start time
     * @param endTime End time
     * @param pageable Pagination information
     * @return Page of product sales statistics
     */
    Page<ProductSalesVO> findTopSellingProducts(
            LocalDateTime startTime,
            LocalDateTime endTime,
            Pageable pageable);

    /**
     * Get shop sales statistics within a time range
     * @param startTime Start time
     * @param endTime End time
     * @param pageable Pagination information
     * @return Page of shop sales statistics
     */
    Page<ShopSalesVO> calculateShopSales(
            LocalDateTime startTime,
            LocalDateTime endTime,
            Pageable pageable);
    
    /**
     * Save an order item
     * @param orderItem OrderItem to save
     * @return Saved order item
     */
    OrderItem save(OrderItem orderItem);
    
    /**
     * Find order item by ID
     * @param orderItemId Order item ID
     * @return Order item
     */
    OrderItem findById(Long orderItemId);
    
    /**
     * Delete order item by ID
     * @param orderItemId Order item ID
     */
    void deleteById(Long orderItemId);
}
