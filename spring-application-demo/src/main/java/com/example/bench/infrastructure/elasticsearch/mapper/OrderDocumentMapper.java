package com.example.bench.infrastructure.elasticsearch.mapper;

import com.example.bench.entity.Order;
import com.example.bench.infrastructure.elasticsearch.document.OrderDocument;
import com.example.bench.util.DateUtils;
import org.springframework.stereotype.Component;

import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Mapper for converting between Order entity and OrderDocument
 */
@Component
public class OrderDocumentMapper {

    /**
     * Convert Order entity to OrderDocument
     *
     * @param order Order entity
     * @return OrderDocument
     */
    public OrderDocument toDocument(Order order) {
        if (order == null) {
            return null;
        }

        OrderDocument document = OrderDocument.builder()
                .orderId(order.getOrderId())
                .shopId(order.getShopId())
                .userId(order.getUserId())
                .deliveryType(order.getDeliveryType() != null ? order.getDeliveryType().longValue() : null)
                .shopName(order.getShopName())
                .total(order.getTotal())
                .status(order.getStatus() != null ? order.getStatus().longValue() : null)
                .allCount(order.getAllCount() != null ? order.getAllCount().longValue() : null)
                .deleteStatus(order.getDeleteStatus() != null ? order.getDeleteStatus().longValue() : null)
                .version(order.getVersion() != null ? order.getVersion().longValue() : null)
                .build();

        // Set date fields using the conversion methods
        if (order.getCreateTime() != null) {
            document.setCreateTime(order.getCreateTime().toInstant(ZoneOffset.UTC));
        }

        if (order.getUpdateTime() != null) {
            document.setUpdateTime(order.getUpdateTime().toInstant(ZoneOffset.UTC));
        }

        if (order.getPayTime() != null) {
            document.setPayTime(order.getPayTime().toInstant(ZoneOffset.UTC));
        }

        if (order.getDeliveryTime() != null) {
            document.setDeliveryTime(order.getDeliveryTime().toInstant(ZoneOffset.UTC));
        }

        if (order.getFinallyTime() != null) {
            document.setFinallyTime(order.getFinallyTime().toInstant(ZoneOffset.UTC));
        }

        // Set boolean fields
        if (order.getIsPayed() != null) {
            document.setIsPayed(order.getIsPayed() ? 1 : 0);
        }

        // Set order address ID if available
        if (order.getOrderAddr() != null) {
            document.setOrderAddrId(order.getOrderAddr().getOrderAddrId());
        }

        return document;
    }

    /**
     * Convert OrderDocument to Order entity
     *
     * @param document OrderDocument
     * @return Order entity
     */
    public Order toEntity(OrderDocument document) {
        if (document == null) {
            return null;
        }

        Order order = new Order();
        order.setOrderId(document.getOrderId());
        order.setShopId(document.getShopId());
        order.setUserId(document.getUserId());
        order.setDeliveryType(document.getDeliveryType() != null ? document.getDeliveryType().intValue() : null);
        order.setShopName(document.getShopName());
        order.setTotal(document.getTotal());
        order.setStatus(document.getStatus() != null ? document.getStatus().intValue() : null);
        order.setAllCount(document.getAllCount() != null ? document.getAllCount().intValue() : null);
        order.setDeleteStatus(document.getDeleteStatus() != null ? document.getDeleteStatus().intValue() : null);
        order.setVersion(document.getVersion() != null ? document.getVersion().intValue() : null);

        // Get date fields using the conversion methods
        order.setCreateTime(DateUtils.instantToLocalDateTime(document.getCreateTime()));
        order.setUpdateTime(DateUtils.instantToLocalDateTime(document.getUpdateTime()));
        order.setPayTime(DateUtils.instantToLocalDateTime(document.getPayTime()));
        order.setDeliveryTime(DateUtils.instantToLocalDateTime(document.getDeliveryTime()));
        order.setFinallyTime(DateUtils.instantToLocalDateTime(document.getFinallyTime()));
        order.setSettledTime(DateUtils.instantToLocalDateTime(document.getSettledTime()));
        order.setCancelTime(DateUtils.instantToLocalDateTime(document.getCancelTime()));

        // Get boolean fields
        order.setIsPayed(document.getIsPayed() == 1);

        // Note: OrderAddr is not fully mapped here, as it would require
        // fetching the actual OrderAddr entity from its repository
        // This would be handled in the service layer or repository implementation

        return order;
    }

    /**
     * Convert a list of Order entities to OrderDocuments
     *
     * @param orders List of Order entities
     * @return List of OrderDocuments
     */
    public List<OrderDocument> toDocuments(List<Order> orders) {
        if (orders == null) {
            return List.of();
        }
        return orders.stream()
                .map(this::toDocument)
                .collect(Collectors.toList());
    }

    /**
     * Convert a list of OrderDocuments to Order entities
     *
     * @param documents Iterable of OrderDocuments
     * @return List of Order entities
     */
    public List<Order> toEntities(Iterable<? extends OrderDocument> documents) {
        if (documents == null) {
            return List.of();
        }
        return StreamSupport.stream(documents.spliterator(), false)
                .map(this::toEntity)
                .collect(Collectors.toList());
    }
}
