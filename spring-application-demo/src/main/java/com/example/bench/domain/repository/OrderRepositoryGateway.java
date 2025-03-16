package com.example.bench.domain.repository;

import com.example.bench.entity.Order;
import com.example.bench.vo.OrderStatVO;
import com.example.bench.vo.ProductSalesVO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Gateway interface for Order repository implementations
 * This follows the Repository pattern from DDD
 */
public interface OrderRepositoryGateway {

    /**
     * Find orders by multiple criteria
     * @param userId User ID (optional)
     * @param status Order status (optional)
     * @param shopName Shop name for partial matching (optional)
     * @param isPayed Payment status (optional)
     * @param minTotal Minimum total amount (optional)
     * @param maxTotal Maximum total amount (optional)
     * @param startTime Start time for order creation (optional)
     * @param endTime End time for order creation (optional)
     * @param pageable Pagination information
     * @return Page of orders matching the criteria
     */
    Page<Order> findOrders(
            Long userId,
            Integer status,
            String shopName,
            Boolean isPayed,
            Long minTotal,
            Long maxTotal,
            LocalDateTime startTime,
            LocalDateTime endTime,
            Pageable pageable);

    /**
     * Get order statistics by region
     * @param startTime Start time
     * @param endTime End time
     * @return List of order statistics by region
     */
    List<OrderStatVO> getRegionStats(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * Get hourly order statistics
     * @param startTime Start time
     * @param endTime End time
     * @return List of hourly order counts
     */
    List<Object[]> countOrdersByHour(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * Find orders by user ID
     * @param userId User ID
     * @return List of orders
     */
    List<Order> findByUserId(Long userId);

    /**
     * Find orders by user ID and status
     * @param userId User ID
     * @param status Order status
     * @return List of orders
     */
    List<Order> findByUserIdAndStatus(Long userId, Integer status);

    /**
     * Find orders by shop ID
     * @param shopId Shop ID
     * @return List of orders
     */
    List<Order> findByShopId(Long shopId);

    /**
     * Find payed orders by time range
     * @param startTime Start time
     * @param endTime End time
     * @return List of orders
     */
    List<Order> findPayedOrders(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * Save an order
     * @param order Order to save
     * @return Saved order
     */
    Order save(Order order);

    /**
     * Find order by ID
     * @param orderId Order ID
     * @return Order
     */
    Order findById(Long orderId);

    /**
     * Delete order by ID
     * @param orderId Order ID
     */
    void deleteById(Long orderId);
}
