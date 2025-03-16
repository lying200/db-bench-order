package com.example.bench.infrastructure.elasticsearch.document;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * Elasticsearch document for Order entity
 * Adjusted to match the existing Elasticsearch index mapping
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "order")
public class OrderDocument {

    @Id
    @Field(name = "order_id", type = FieldType.Long)
    private Long orderId;

    @Field(name = "create_time", type = FieldType.Date)
    private Instant createTime;

    @Field(name = "update_time", type = FieldType.Date)
    private Instant updateTime;

    @Field(name = "shop_id", type = FieldType.Long)
    private Long shopId;

    @Field(name = "user_id", type = FieldType.Long)
    private Long userId;

    @Field(name = "delivery_type", type = FieldType.Long)
    private Long deliveryType;

    @Field(name = "shop_name", type = FieldType.Text)
    private String shopName;

    @Field(name = "total", type = FieldType.Long)
    private Long total;

    @Field(name = "status", type = FieldType.Long)
    private Long status;

    @Field(name = "all_count", type = FieldType.Long)
    private Long allCount;

    @Field(name = "pay_time", type = FieldType.Date)
    private Instant payTime;

    @Field(name = "delivery_time", type = FieldType.Date)
    private Instant deliveryTime;

    @Field(name = "finally_time", type = FieldType.Date)
    private Instant finallyTime;

    @Field(name = "order_addr_id", type = FieldType.Long)
    private Long orderAddrId;

    @Field(name = "settled_time", type = FieldType.Date)
    private Instant settledTime;

    @Field(name = "cancel_time", type = FieldType.Date)
    private Instant cancelTime;

    @Field(name = "is_payed", type = FieldType.Long)
    private Long isPayedValue;

    @Field(name = "is_payed", type = FieldType.Integer)
    private Integer isPayed;

    @Field(name = "close_type", type = FieldType.Long)
    private Integer closeType;

    @Field(name = "delete_status", type = FieldType.Long)
    private Long deleteStatus;

    @Field(name = "version", type = FieldType.Long)
    private Long version;
}
