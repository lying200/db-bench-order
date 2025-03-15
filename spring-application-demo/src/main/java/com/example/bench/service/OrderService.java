package com.example.bench.service;

import com.example.bench.entity.Order;
import com.example.bench.entity.OrderItem;
import com.example.bench.repository.OrderItemRepository;
import com.example.bench.repository.OrderRepository;
import com.example.bench.vo.OrderStatVO;
import com.example.bench.vo.ProductSalesVO;
import com.example.bench.vo.ShopSalesVO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import jakarta.persistence.criteria.Predicate;
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

        Specification<Order> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 用户ID筛选
            if (userId != null) {
                predicates.add(cb.equal(root.get("userId"), userId));
            }

            // 订单状态筛选
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            // 店铺名称模糊搜索
            if (StringUtils.hasText(shopName)) {
                predicates.add(cb.like(root.get("shopName"), "%" + shopName + "%"));
            }

            // 支付状态筛选
            if (isPayed != null) {
                predicates.add(cb.equal(root.get("isPayed"), isPayed));
            }

            // 订单金额范围筛选
            if (minTotal != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("total"), minTotal));
            }
            if (maxTotal != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("total"), maxTotal));
            }

            // 时间范围筛选
            if (startTime != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createTime"), startTime));
            }
            if (endTime != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createTime"), endTime));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return orderRepository.findAll(spec, pageable);
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
