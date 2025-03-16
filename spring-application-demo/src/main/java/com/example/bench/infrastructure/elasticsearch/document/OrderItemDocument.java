package com.example.bench.infrastructure.elasticsearch.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.Instant;

/**
 * Elasticsearch document for OrderItem entity
 * Matches the existing Elasticsearch index mapping
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "order_item")
public class OrderItemDocument {

    @Id
    @Field(name = "order_item_id", type = FieldType.Long)
    private Long orderItemId;

    @Field(name = "create_time", type = FieldType.Date)
    private Instant createTime;

    @Field(name = "update_time", type = FieldType.Date)
    private Instant updateTime;

    @Field(name = "shop_id", type = FieldType.Long)
    private Long shopId;

    @Field(name = "category_id", type = FieldType.Long)
    private Long categoryId;

    @Field(name = "spu_id", type = FieldType.Long)
    private Long spuId;

    @Field(name = "sku_id", type = FieldType.Long)
    private Long skuId;

    @Field(name = "user_id", type = FieldType.Long)
    private Long userId;

    @Field(name = "count", type = FieldType.Long)
    private Long count;

    @Field(name = "spu_name", type = FieldType.Text)
    private String spuName;

    @Field(name = "sku_name", type = FieldType.Text)
    private String skuName;

    @Field(name = "pic", type = FieldType.Text)
    private String pic;

    @Field(name = "price", type = FieldType.Long)
    private Long price;

    @Field(name = "spu_total_amount", type = FieldType.Long)
    private Long spuTotalAmount;

    @Field(name = "order_id", type = FieldType.Long)
    private Long orderId;
}
