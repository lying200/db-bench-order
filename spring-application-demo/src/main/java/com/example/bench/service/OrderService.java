package com.example.bench.service;

import com.example.bench.domain.repository.OrderItemRepositoryGateway;
import com.example.bench.domain.repository.OrderRepositoryGateway;
import com.example.bench.entity.Order;
import com.example.bench.entity.OrderItem;
import com.example.bench.infrastructure.factory.OrderItemRepositoryFactory;
import com.example.bench.infrastructure.factory.OrderRepositoryFactory;
import com.example.bench.vo.OrderStatVO;
import com.example.bench.vo.ProductSalesVO;
import com.example.bench.vo.ShopSalesVO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepositoryFactory orderRepositoryFactory;
    private final OrderItemRepositoryFactory orderItemRepositoryFactory;

    @Transactional(readOnly = true)
    public Page<Order> findOrders(
            Long userId,
            Integer status,
            String shopName,
            Boolean isPayed,
            Long minTotal,
            Long maxTotal,
            LocalDateTime startTime,
            LocalDateTime endTime,
            Pageable pageable) {

        OrderRepositoryGateway orderRepository = orderRepositoryFactory.getOrderRepository();
        return orderRepository.findOrders(
                userId, status, shopName, isPayed, minTotal, maxTotal, startTime, endTime, pageable);
    }

    @Transactional(readOnly = true)
    public List<OrderStatVO> getRegionStats(LocalDateTime startTime, LocalDateTime endTime) {
        OrderRepositoryGateway orderRepository = orderRepositoryFactory.getOrderRepository();
        return orderRepository.getRegionStats(startTime, endTime);
    }

    @Transactional(readOnly = true)
    public Map<Integer, Long> getHourlyStats(LocalDateTime startTime, LocalDateTime endTime) {
        OrderRepositoryGateway orderRepository = orderRepositoryFactory.getOrderRepository();
        return orderRepository.countOrdersByHour(startTime, endTime)
                .stream()
                .collect(Collectors.toMap(
                        result -> (Integer) result[0],
                        result -> (Long) result[1]
                ));
    }

    @Transactional(readOnly = true)
    public Page<ProductSalesVO> getProductSales(LocalDateTime startTime, LocalDateTime endTime, Pageable pageable) {
        OrderItemRepositoryGateway orderItemRepository =
                orderItemRepositoryFactory.getOrderItemRepository();
        return orderItemRepository.findTopSellingProducts(startTime, endTime, pageable);
    }

    @Transactional(readOnly = true)
    public Page<ShopSalesVO> getShopSales(LocalDateTime startTime, LocalDateTime endTime, Pageable pageable) {
        OrderItemRepositoryGateway orderItemRepository =
                orderItemRepositoryFactory.getOrderItemRepository();
        return orderItemRepository.calculateShopSales(startTime, endTime, pageable);
    }

    @Transactional(readOnly = true)
    public List<OrderItem> getOrderItems(Long orderId) {
        OrderItemRepositoryGateway orderItemRepository =
                orderItemRepositoryFactory.getOrderItemRepository();
        return orderItemRepository.findByOrderId(orderId);
    }
}
