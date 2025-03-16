package com.example.bench.infrastructure.elasticsearch.repository;

import com.example.bench.infrastructure.elasticsearch.document.OrderItemDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Elasticsearch repository for OrderItem documents
 */
@Repository
public interface OrderItemElasticsearchRepository extends ElasticsearchRepository<OrderItemDocument, Long> {
    
    /**
     * Find order items by order ID
     * @param orderId Order ID
     * @return List of order item documents
     */
    List<OrderItemDocument> findByOrderId(Long orderId);
}
