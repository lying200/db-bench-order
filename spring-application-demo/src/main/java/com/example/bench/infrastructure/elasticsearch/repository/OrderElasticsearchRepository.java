package com.example.bench.infrastructure.elasticsearch.repository;

import com.example.bench.infrastructure.elasticsearch.document.OrderDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Elasticsearch repository for Order documents
 */
@Repository
public interface OrderElasticsearchRepository extends ElasticsearchRepository<OrderDocument, Long> {
    
    /**
     * Find orders by user ID
     * @param userId User ID
     * @return List of orders
     */
    List<OrderDocument> findByUserIdOrderByCreateTimeDesc(Long userId);
    
    /**
     * Find orders by user ID and status
     * @param userId User ID
     * @param status Order status
     * @return List of orders
     */
    List<OrderDocument> findByUserIdAndStatusOrderByCreateTimeDesc(Long userId, Integer status);
    
    /**
     * Find orders by user ID with pagination
     * @param userId User ID
     * @param pageable Pagination information
     * @return Paginated orders
     */
    Page<OrderDocument> findByUserId(Long userId, Pageable pageable);
    
    /**
     * Find orders by user ID and status with pagination
     * @param userId User ID
     * @param status Order status
     * @param pageable Pagination information
     * @return Paginated orders
     */
    Page<OrderDocument> findByUserIdAndStatus(Long userId, Integer status, Pageable pageable);
    
    /**
     * Find orders by shop ID
     * @param shopId Shop ID
     * @return List of orders
     */
    List<OrderDocument> findByShopIdOrderByCreateTimeDesc(Long shopId);
    
    /**
     * Find orders by shop name containing the given text
     * @param shopName Shop name to search for
     * @return List of orders
     */
    List<OrderDocument> findByShopNameContaining(String shopName);
    
    /**
     * Find orders created between the given time range
     * @param startTime Start time
     * @param endTime End time
     * @return List of orders
     */
    List<OrderDocument> findByCreateTimeBetween(LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * Find paid orders within a time range
     * @param startTime Start time
     * @param endTime End time
     * @return List of paid orders
     */
    List<OrderDocument> findByPayTimeBetweenAndIsPayedTrue(LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * Custom query to find orders with total amount between min and max
     * @param minTotal Minimum total amount
     * @param maxTotal Maximum total amount
     * @return List of orders
     */
    @Query("{\"range\": {\"total\": {\"gte\": ?0, \"lte\": ?1}}}")
    List<OrderDocument> findByTotalBetween(Long minTotal, Long maxTotal);
}
