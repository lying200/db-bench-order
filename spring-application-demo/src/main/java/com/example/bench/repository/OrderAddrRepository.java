package com.example.bench.repository;

import com.example.bench.entity.OrderAddr;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface OrderAddrRepository extends JpaRepository<OrderAddr, Long> {
    
    List<OrderAddr> findByUserId(Long userId);
    
    // 统计各省份订单地址数量
    @Query("SELECT oa.province, COUNT(oa) as count FROM OrderAddr oa GROUP BY oa.province")
    List<Object[]> countAddressByProvince();
    
    // 查找某个城市范围内的地址
    @Query("SELECT oa FROM OrderAddr oa " +
           "WHERE oa.lng BETWEEN :minLng AND :maxLng " +
           "AND oa.lat BETWEEN :minLat AND :maxLat")
    List<OrderAddr> findAddressInArea(Double minLng, Double maxLng, Double minLat, Double maxLat);
}
