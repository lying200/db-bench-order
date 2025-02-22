package com.example.bench.repository;

import com.example.bench.entity.Order;
import com.example.bench.vo.OrderStatVO;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单数据访问层
 * 
 * @author [Your Name]
 * @since [Version]
 */
@Tag(name = "订单数据访问层", description = "订单相关的数据库操作")
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    /**
     * 查询用户订单列表
     * 
     * @param userId 用户ID
     * @param status 订单状态（可选）
     * @return 订单列表
     */
    List<Order> findByUserIdAndStatusOrderByCreateTimeDesc(Long userId, Integer status);
    List<Order> findByUserIdOrderByCreateTimeDesc(Long userId);
    
    /**
     * 分页查询用户订单列表
     * @param userId 用户ID
     * @param pageable 分页参数
     * @return 订单分页列表
     */
    Page<Order> findByUserId(Long userId, Pageable pageable);

    /**
     * 分页查询用户指定状态的订单列表
     * @param userId 用户ID
     * @param status 订单状态
     * @param pageable 分页参数
     * @return 订单分页列表
     */
    Page<Order> findByUserIdAndStatus(Long userId, Integer status, Pageable pageable);
    
    /**
     * 查询店铺订单列表
     * 
     * @param shopId 店铺ID
     * @return 订单列表
     */
    List<Order> findByShopIdOrderByCreateTimeDesc(Long shopId);
    
    /**
     * 按小时统计订单数量
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 每小时订单数量
     */
    @Query("SELECT HOUR(o.createTime) as hour, COUNT(o) as count " +
           "FROM Order o " +
           "WHERE o.createTime BETWEEN :startTime AND :endTime " +
           "GROUP BY HOUR(o.createTime)")
    List<Object[]> countOrdersByHour(@Param("startTime") LocalDateTime startTime,
                                    @Param("endTime") LocalDateTime endTime);
    
    /**
     * 按地区统计订单
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 各地区订单统计
     */
    @Query("SELECT new com.example.bench.vo.OrderStatVO(o.orderAddr.province, COUNT(o)) " +
           "FROM Order o " +
           "WHERE o.createTime BETWEEN :startTime AND :endTime " +
           "GROUP BY o.orderAddr.province")
    List<OrderStatVO> countOrdersByRegion(@Param("startTime") LocalDateTime startTime,
                                         @Param("endTime") LocalDateTime endTime);
    
    /**
     * 查询指定时间范围内的已支付订单
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 已支付订单列表
     */
    @Query("SELECT o FROM Order o WHERE o.payTime BETWEEN :startTime AND :endTime AND o.isPayed = true")
    List<Order> findPayedOrders(@Param("startTime") LocalDateTime startTime,
                               @Param("endTime") LocalDateTime endTime);
}
