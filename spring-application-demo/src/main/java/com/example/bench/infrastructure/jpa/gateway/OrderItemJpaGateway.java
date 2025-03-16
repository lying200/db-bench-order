package com.example.bench.infrastructure.jpa.gateway;

import com.example.bench.domain.repository.OrderItemRepositoryGateway;
import com.example.bench.entity.OrderItem;
import com.example.bench.infrastructure.jpa.repository.OrderItemRepository;
import com.example.bench.vo.ProductSalesVO;
import com.example.bench.vo.ShopSalesVO;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * JPA implementation of the OrderItemRepositoryGateway
 */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.repository.type", havingValue = "jpa")
public class OrderItemJpaGateway implements OrderItemRepositoryGateway {

    private final OrderItemRepository orderItemRepository;

    @Override
    public List<OrderItem> findByOrderId(Long orderId) {
        return orderItemRepository.findByOrderId(orderId);
    }

    @Override
    public Page<ProductSalesVO> findTopSellingProducts(
            LocalDateTime startTime,
            LocalDateTime endTime,
            Pageable pageable) {
        return orderItemRepository.findTopSellingProducts(startTime, endTime, pageable);
    }

    @Override
    public Page<ShopSalesVO> calculateShopSales(
            LocalDateTime startTime,
            LocalDateTime endTime,
            Pageable pageable) {
        return orderItemRepository.calculateShopSales(startTime, endTime, pageable);
    }

    @Override
    public OrderItem save(OrderItem orderItem) {
        return orderItemRepository.save(orderItem);
    }

    @Override
    public OrderItem findById(Long orderItemId) {
        return orderItemRepository.findById(orderItemId).orElse(null);
    }

    @Override
    public void deleteById(Long orderItemId) {
        orderItemRepository.deleteById(orderItemId);
    }
}
