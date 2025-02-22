package com.example.bench.service;

import com.example.bench.entity.Order;
import com.example.bench.entity.OrderItem;
import com.example.bench.repository.OrderAddrRepository;
import com.example.bench.repository.OrderItemRepository;
import com.example.bench.repository.OrderRepository;
import com.example.bench.vo.OrderStatVO;
import com.example.bench.vo.ProductSalesVO;
import com.example.bench.vo.ShopSalesVO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    @Transactional(readOnly = true)
    public Page<Order> findOrders(Long userId, Integer status, Pageable pageable) {
        if (userId != null) {
            if (status != null) {
                return orderRepository.findByUserIdAndStatus(userId, status, pageable);
            }
            return orderRepository.findByUserId(userId, pageable);
        }
        return orderRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public List<OrderStatVO> getRegionStats(LocalDateTime startTime, LocalDateTime endTime) {
        return orderRepository.countOrdersByRegion(startTime, endTime);
    }

    @Transactional(readOnly = true)
    public Map<Integer, Long> getHourlyStats(LocalDateTime startTime, LocalDateTime endTime) {
        return orderRepository.countOrdersByHour(startTime, endTime)
                .stream()
                .collect(Collectors.toMap(
                        result -> (Integer) result[0],
                        result -> (Long) result[1]
                ));
    }

    @Transactional(readOnly = true)
    public Page<ProductSalesVO> getProductSales(LocalDateTime startTime, LocalDateTime endTime, Pageable pageable) {
        return orderItemRepository.findTopSellingProducts(startTime, endTime, pageable);
    }

    @Transactional(readOnly = true)
    public Page<ShopSalesVO> getShopSales(LocalDateTime startTime, LocalDateTime endTime, Pageable pageable) {
        return orderItemRepository.calculateShopSales(startTime, endTime, pageable);
    }

    @Transactional(readOnly = true)
    public List<OrderItem> getOrderItems(Long orderId) {
        return orderItemRepository.findByOrderId(orderId);
    }
}
