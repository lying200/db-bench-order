package com.example.bench.infrastructure.jpa.gateway;

import com.example.bench.domain.repository.OrderRepositoryGateway;
import com.example.bench.entity.Order;
import com.example.bench.infrastructure.jpa.repository.OrderRepository;
import com.example.bench.vo.OrderStatVO;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import jakarta.persistence.criteria.Predicate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JPA implementation of the OrderRepositoryGateway
 */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.repository.type", havingValue = "jpa")
public class OrderJpaGateway implements OrderRepositoryGateway {

    private final OrderRepository orderRepository;

    @Override
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

    @Override
    public List<OrderStatVO> getRegionStats(LocalDateTime startTime, LocalDateTime endTime) {
        return orderRepository.countOrdersByRegion(startTime, endTime);
    }

    @Override
    public List<Object[]> countOrdersByHour(LocalDateTime startTime, LocalDateTime endTime) {
        return orderRepository.countOrdersByHour(startTime, endTime);
    }

    @Override
    public List<Order> findByUserId(Long userId) {
        return orderRepository.findByUserIdOrderByCreateTimeDesc(userId);
    }

    @Override
    public List<Order> findByUserIdAndStatus(Long userId, Integer status) {
        return orderRepository.findByUserIdAndStatusOrderByCreateTimeDesc(userId, status);
    }

    @Override
    public List<Order> findByShopId(Long shopId) {
        return orderRepository.findByShopIdOrderByCreateTimeDesc(shopId);
    }

    @Override
    public List<Order> findPayedOrders(LocalDateTime startTime, LocalDateTime endTime) {
        return orderRepository.findPayedOrders(startTime, endTime);
    }

    @Override
    public Order save(Order order) {
        return orderRepository.save(order);
    }

    @Override
    public Order findById(Long orderId) {
        Optional<Order> order = orderRepository.findById(orderId);
        return order.orElse(null);
    }

    @Override
    public void deleteById(Long orderId) {
        orderRepository.deleteById(orderId);
    }
}
