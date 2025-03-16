package com.example.bench.infrastructure.elasticsearch.mapper;

import com.example.bench.entity.OrderItem;
import com.example.bench.infrastructure.elasticsearch.document.OrderItemDocument;
import com.example.bench.util.DateUtils;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Mapper for converting between OrderItem entity and OrderItemDocument
 */
@Component
public class OrderItemDocumentMapper {

    /**
     * Convert LocalDateTime to milliseconds since epoch
     *
     * @param dateTime LocalDateTime to convert
     * @return milliseconds since epoch
     */
    private Long toEpochMillis(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.toInstant(ZoneOffset.UTC).toEpochMilli();
    }

    /**
     * Convert OrderItem entity to OrderItemDocument
     *
     * @param orderItem OrderItem entity
     * @return OrderItemDocument
     */
    public OrderItemDocument toDocument(OrderItem orderItem) {
        if (orderItem == null) {
            return null;
        }

        OrderItemDocument document = OrderItemDocument.builder()
                .orderItemId(orderItem.getOrderItemId())
                .shopId(orderItem.getShopId())
                .categoryId(orderItem.getCategoryId())
                .spuId(orderItem.getSpuId())
                .skuId(orderItem.getSkuId())
                .userId(orderItem.getUserId())
                .count(orderItem.getCount() != null ? orderItem.getCount().longValue() : null)
                .spuName(orderItem.getSpuName())
                .skuName(orderItem.getSkuName())
                .pic(orderItem.getPic())
                .price(orderItem.getPrice())
                .spuTotalAmount(orderItem.getSpuTotalAmount())
                .build();

        // Set date fields using the conversion methods
        document.setCreateTime(orderItem.getCreateTime().toInstant(ZoneOffset.UTC));
        document.setUpdateTime(orderItem.getUpdateTime().toInstant(ZoneOffset.UTC));

        // Set order ID if available
        if (orderItem.getOrder() != null) {
            document.setOrderId(orderItem.getOrder().getOrderId());
        }

        return document;
    }

    /**
     * Convert OrderItemDocument to OrderItem entity
     *
     * @param document OrderItemDocument
     * @return OrderItem entity
     */
    public OrderItem toEntity(OrderItemDocument document) {
        if (document == null) {
            return null;
        }

        OrderItem orderItem = new OrderItem();
        orderItem.setOrderItemId(document.getOrderItemId());
        orderItem.setShopId(document.getShopId());
        orderItem.setCategoryId(document.getCategoryId());
        orderItem.setSpuId(document.getSpuId());
        orderItem.setSkuId(document.getSkuId());
        orderItem.setUserId(document.getUserId());
        orderItem.setCount(document.getCount() != null ? document.getCount().intValue() : null);
        orderItem.setSpuName(document.getSpuName());
        orderItem.setSkuName(document.getSkuName());
        orderItem.setPic(document.getPic());
        orderItem.setPrice(document.getPrice());
        orderItem.setSpuTotalAmount(document.getSpuTotalAmount());

        // Set date fields using the conversion methods
        orderItem.setCreateTime(DateUtils.instantToLocalDateTime(document.getCreateTime()));
        orderItem.setUpdateTime(DateUtils.instantToLocalDateTime(document.getUpdateTime()));

        // Note: Order is not fully mapped here, as it would require
        // fetching the actual Order entity from its repository
        // This would be handled in the service layer or repository implementation

        return orderItem;
    }

    /**
     * Convert a list of OrderItem entities to OrderItemDocuments
     *
     * @param orderItems List of OrderItem entities
     * @return List of OrderItemDocuments
     */
    public List<OrderItemDocument> toDocuments(List<OrderItem> orderItems) {
        if (orderItems == null) {
            return List.of();
        }
        return orderItems.stream()
                .map(this::toDocument)
                .collect(Collectors.toList());
    }

    /**
     * Convert a list of OrderItemDocuments to OrderItem entities
     *
     * @param documents Iterable of OrderItemDocuments
     * @return List of OrderItem entities
     */
    public List<OrderItem> toEntities(Iterable<? extends OrderItemDocument> documents) {
        if (documents == null) {
            return List.of();
        }
        return StreamSupport.stream(documents.spliterator(), false)
                .map(this::toEntity)
                .collect(Collectors.toList());
    }
}
