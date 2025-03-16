package com.example.bench.infrastructure.elasticsearch.gateway;

import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregation;
import co.elastic.clients.elasticsearch._types.aggregations.StringTermsAggregate;
import co.elastic.clients.elasticsearch._types.aggregations.StringTermsBucket;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders;
import co.elastic.clients.json.JsonData;
import co.elastic.clients.util.NamedValue;
import com.example.bench.domain.repository.OrderItemRepositoryGateway;
import com.example.bench.entity.OrderItem;
import com.example.bench.infrastructure.elasticsearch.document.OrderItemDocument;
import com.example.bench.infrastructure.elasticsearch.mapper.OrderItemDocumentMapper;
import com.example.bench.infrastructure.elasticsearch.repository.OrderItemElasticsearchRepository;
import com.example.bench.vo.ProductSalesVO;
import com.example.bench.vo.ShopSalesVO;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.*;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchAggregation;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchAggregations;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.elasticsearch.core.AggregationsContainer;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Elasticsearch implementation of the OrderItemRepositoryGateway
 */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.repository.type", havingValue = "elasticsearch")
public class OrderItemElasticsearchGateway implements OrderItemRepositoryGateway {

    private final OrderItemElasticsearchRepository elasticsearchRepository;
    private final OrderItemDocumentMapper mapper;
    private final ElasticsearchOperations elasticsearchOperations;

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

    @Override
    public List<OrderItem> findByOrderId(Long orderId) {
        NativeQueryBuilder queryBuilder = new NativeQueryBuilder();
        queryBuilder.withQuery(q -> q.term(t -> t.field("order_id").value(orderId)));
        queryBuilder.withSort(s -> s.field(f -> f.field("create_time").order(SortOrder.Desc)));

        NativeQuery searchQuery = queryBuilder.build();
        SearchHits<OrderItemDocument> searchHits = elasticsearchOperations.search(
                searchQuery, OrderItemDocument.class);

        List<OrderItemDocument> documents = searchHits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .collect(Collectors.toList());

        return mapper.toEntities(documents);
    }

    @Override
    public Page<ProductSalesVO> findTopSellingProducts(
            LocalDateTime startTime,
            LocalDateTime endTime,
            Pageable pageable) {

        // 构建查询条件
        BoolQuery.Builder boolQuery = new BoolQuery.Builder();

        // 添加时间范围条件
        if (startTime != null) {
            Long startTimeMillis = toEpochMillis(startTime);
            boolQuery.filter(f -> f.range(r -> r.field("create_time").gte(JsonData.of(startTimeMillis))));
        }

        if (endTime != null) {
            Long endTimeMillis = toEpochMillis(endTime);
            boolQuery.filter(f -> f.range(r -> r.field("create_time").lte(JsonData.of(endTimeMillis))));
        }

        // 构建完整查询
        Query query = NativeQuery.builder()
                .withMaxResults(0)
                .withQuery(q -> q.bool(boolQuery.build()))
                .withAggregation("group_by_spuId", Aggregation.of(a -> a
                        .terms(term -> term
                                .field("spu_id")
                                .size(100)
                                .order(List.of(NamedValue.of("total_count", SortOrder.Desc)))
                        )
                        .aggregations(Map.of(
                                "group_by_spuName", Aggregation.of(subAgg -> subAgg
                                        .terms(term -> term
                                                .field("spu_name.keyword")
                                                .size(1)
                                        )
                                ),
                                "total_count", Aggregation.of(subAgg -> subAgg
                                        .sum(sum -> sum.field("count"))
                                ),
                                "total_amount", Aggregation.of(subAgg -> subAgg
                                        .sum(sum -> sum.field("spu_total_amount"))
                                )
                        ))
                ))
                .build();

        // 执行查询
        SearchHits<?> searchHits = elasticsearchOperations.search(query, OrderItemDocument.class);

        // 处理聚合结果
        ElasticsearchAggregation aggregation;

        AggregationsContainer<?> aggs = searchHits.getAggregations();
        if (!(aggs instanceof ElasticsearchAggregations)
                || (aggregation = (((ElasticsearchAggregations) aggs).get("group_by_spuId"))) == null) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }

        List<ProductSalesVO> productSalesVOS =
                aggregation.aggregation().getAggregate().lterms().buckets().array().stream().map(bucket -> {
                    long spuId = bucket.key();
                    StringTermsBucket groupBySpuName =
                            bucket.aggregations().get("group_by_spuName").sterms().buckets().array().get(0);
                    String spuName = groupBySpuName.key().stringValue();
                    long totalCount =
                            Double.valueOf(bucket.aggregations().get("total_count").sum().value()).longValue();
                    long totalAmount =
                            Double.valueOf(bucket.aggregations().get("total_amount").sum().value()).longValue();

                    return new ProductSalesVO(spuId, spuName, totalCount, totalAmount);
                }).toList();

        return new PageImpl<>(
                productSalesVOS.subList(pageable.getPageNumber() * pageable.getPageSize(),
                                        (pageable.getPageNumber() + 1) * pageable.getPageSize()),
                pageable,
                productSalesVOS.size()
        );
    }

    @Override
    public Page<ShopSalesVO> calculateShopSales(
            LocalDateTime startTime,
            LocalDateTime endTime,
            Pageable pageable) {

        // Build Elasticsearch query for time range
        BoolQuery.Builder boolBuilder = QueryBuilders.bool();
        if (startTime != null) {

            Long startTimeMillis = toEpochMillis(startTime);
            boolBuilder.must(QueryBuilders.range(ta -> ta.field("create_time").gte(JsonData.of(startTimeMillis))));
        }

        if (endTime != null) {
            Long endTimeMillis = toEpochMillis(endTime);
            boolBuilder.must(QueryBuilders.range(ta -> ta.field("create_time").lte(JsonData.of(endTimeMillis))));
        }

        NativeQueryBuilder queryBuilder = new NativeQueryBuilder();
        queryBuilder.withQuery(builder -> builder.bool(boolBuilder.build()));
        queryBuilder.withMaxResults(0);

        // Add terms aggregation on shop_id with sub-aggregations for total amount sum and order count
        queryBuilder.withAggregation("by_shop", Aggregation.of(a -> a
                .terms(t -> t.field("shop_id").size(100).order(List.of(NamedValue.of("total_amount", SortOrder.Desc)))) // Large size to get all buckets
                .aggregations(Map.of(
                        "total_amount", Aggregation.of(sa -> sa.sum(s -> s.field("spu_total_amount"))),
                        "order_count", Aggregation.of(sa -> sa.cardinality(c -> c.field("order_id")))
                ))
        ));

        NativeQuery searchQuery = queryBuilder.build();
        SearchHits<OrderItemDocument> searchHits = elasticsearchOperations.search(
                searchQuery, OrderItemDocument.class);

        // 处理聚合结果
        ElasticsearchAggregation aggregation;

        AggregationsContainer<?> aggs = searchHits.getAggregations();
        if (!(aggs instanceof ElasticsearchAggregations)
                || (aggregation = (((ElasticsearchAggregations) aggs).get("by_shop"))) == null) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }

        List<ShopSalesVO> shopSales = aggregation.aggregation().getAggregate().lterms().buckets().array().stream()
                .map(bucket -> {
                    long shopId = bucket.key();
                    long totalAmount =
                            Double.valueOf(bucket.aggregations().get("total_amount").sum().value()).longValue();
                    long orderCount = bucket.aggregations().get("order_count").cardinality().value();
                    return new ShopSalesVO(shopId, totalAmount, orderCount);
                }).toList();

        int start = pageable.getPageNumber() * pageable.getPageSize();
        int end = Math.min(start + pageable.getPageSize(), shopSales.size());

        return new PageImpl<>(
                shopSales.subList(start, end),
                pageable,
                shopSales.size()
        );
    }

    @Override
    public OrderItem save(OrderItem orderItem) {
        OrderItemDocument document = mapper.toDocument(orderItem);
        OrderItemDocument savedDocument = elasticsearchRepository.save(document);
        return mapper.toEntity(savedDocument);
    }

    @Override
    public OrderItem findById(Long orderItemId) {
        Optional<OrderItemDocument> document = elasticsearchRepository.findById(orderItemId);
        return document.map(mapper::toEntity).orElse(null);
    }

    @Override
    public void deleteById(Long orderItemId) {
        elasticsearchRepository.deleteById(orderItemId);
    }
}
