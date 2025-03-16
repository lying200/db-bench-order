package com.example.bench.infrastructure.elasticsearch.gateway;

import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders;
import co.elastic.clients.json.JsonData;
import com.example.bench.domain.repository.OrderRepositoryGateway;
import com.example.bench.entity.Order;
import com.example.bench.infrastructure.elasticsearch.document.OrderDocument;
import com.example.bench.infrastructure.elasticsearch.mapper.OrderDocumentMapper;
import com.example.bench.infrastructure.elasticsearch.repository.OrderElasticsearchRepository;
import com.example.bench.vo.OrderStatVO;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoField;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalField;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Elasticsearch implementation of the OrderRepositoryGateway
 */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.repository.type", havingValue = "elasticsearch")
public class OrderElasticsearchGateway implements OrderRepositoryGateway {

    private final OrderElasticsearchRepository elasticsearchRepository;
    private final OrderDocumentMapper mapper;
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

        // Build Elasticsearch query
        BoolQuery.Builder boolBuilder = QueryBuilders.bool();
        if (userId != null) {
            boolBuilder.must(QueryBuilders.term(ta -> ta.field("user_id").value(userId)));
        }

        if (status != null) {
            boolBuilder.must(QueryBuilders.term(ta -> ta.field("status").value(status.longValue())));
        }

        if (shopName != null && !shopName.isEmpty()) {
            boolBuilder.must(QueryBuilders.match(ta -> ta.field("shop_name").query(shopName)));
        }

        if (isPayed != null) {
            long isPayedValue = isPayed ? 1L : 0L;
            boolBuilder.must(QueryBuilders.term(ta -> ta.field("is_payed").value(isPayedValue)));
        }

        if (minTotal != null) {
            boolBuilder.must(QueryBuilders.range(ta -> ta.field("total").gte(JsonData.of(minTotal))));
        }

        if (maxTotal != null) {
            boolBuilder.must(QueryBuilders.range(ta -> ta.field("total").lte(JsonData.of(maxTotal))));
        }

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

        // Apply pagination and sorting
        queryBuilder.withPageable(pageable);

        // Always add default sort by createTime desc if no sort is specified in pageable
        if (pageable.getSort().isEmpty()) {
            queryBuilder.withSort(sort -> sort.field(f -> f.field("create_time").order(SortOrder.Desc)));
        }

        NativeQuery searchQuery = queryBuilder.build();

        SearchHits<OrderDocument> searchHits = elasticsearchOperations.search(
                searchQuery, OrderDocument.class);

        List<OrderDocument> documents = searchHits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .collect(Collectors.toList());

        List<Order> orders = mapper.toEntities(documents);

        return new PageImpl<>(orders, pageable, searchHits.getTotalHits());
    }

    @Override
    public List<OrderStatVO> getRegionStats(LocalDateTime startTime, LocalDateTime endTime) {
        // Since we no longer have province field in the document, we need to handle this differently
        // For now, we'll return an empty list as this would require additional data
        return List.of();
    }

    @Override
    public List<Object[]> countOrdersByHour(LocalDateTime startTime, LocalDateTime endTime) {
        // For Elasticsearch, we need to use aggregations
        // This is a simplified implementation, in a real-world scenario
        // you would use Elasticsearch aggregations
        Long startTimeMillis = toEpochMillis(startTime);
        Long endTimeMillis = toEpochMillis(endTime);

        NativeQueryBuilder queryBuilder = new NativeQueryBuilder();
        BoolQuery.Builder boolBuilder = QueryBuilders.bool();

        if (startTimeMillis != null) {
            boolBuilder.must(QueryBuilders.range(ta -> ta.field("create_time").gte(JsonData.of(startTimeMillis))));
        }

        if (endTimeMillis != null) {
            boolBuilder.must(QueryBuilders.range(ta -> ta.field("create_time").lte(JsonData.of(endTimeMillis))));
        }

        queryBuilder.withQuery(builder -> builder.bool(boolBuilder.build()));
        NativeQuery searchQuery = queryBuilder.build();

        SearchHits<OrderDocument> searchHits = elasticsearchOperations.search(
                searchQuery, OrderDocument.class);

        List<OrderDocument> documents = searchHits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .toList();

        // Group by hour and count
        return documents.stream()
                .filter(doc -> doc.getCreateTime() != null)
                .collect(Collectors.groupingBy(
                        doc -> doc.getCreateTime().get(ChronoField.HOUR_OF_DAY),
                        Collectors.counting()))
                .entrySet().stream()
                .map(entry -> new Object[]{entry.getKey(), entry.getValue()})
                .collect(Collectors.toList());
    }

    @Override
    public List<Order> findByUserId(Long userId) {
        NativeQueryBuilder queryBuilder = new NativeQueryBuilder();
        queryBuilder.withQuery(q -> q.term(t -> t.field("user_id").value(userId)));
        queryBuilder.withSort(s -> s.field(f -> f.field("create_time").order(SortOrder.Desc)));

        NativeQuery searchQuery = queryBuilder.build();
        SearchHits<OrderDocument> searchHits = elasticsearchOperations.search(
                searchQuery, OrderDocument.class);

        List<OrderDocument> documents = searchHits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .collect(Collectors.toList());

        return mapper.toEntities(documents);
    }

    @Override
    public List<Order> findByUserIdAndStatus(Long userId, Integer status) {
        NativeQueryBuilder queryBuilder = new NativeQueryBuilder();
        BoolQuery.Builder boolBuilder = QueryBuilders.bool();

        boolBuilder.must(QueryBuilders.term(ta -> ta.field("user_id").value(userId)));
        boolBuilder.must(QueryBuilders.term(ta -> ta.field("status").value(status.longValue())));

        queryBuilder.withQuery(builder -> builder.bool(boolBuilder.build()));
        queryBuilder.withSort(s -> s.field(f -> f.field("create_time").order(SortOrder.Desc)));

        NativeQuery searchQuery = queryBuilder.build();
        SearchHits<OrderDocument> searchHits = elasticsearchOperations.search(
                searchQuery, OrderDocument.class);

        List<OrderDocument> documents = searchHits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .collect(Collectors.toList());

        return mapper.toEntities(documents);
    }

    @Override
    public List<Order> findByShopId(Long shopId) {
        NativeQueryBuilder queryBuilder = new NativeQueryBuilder();
        queryBuilder.withQuery(q -> q.term(t -> t.field("shop_id").value(shopId)));
        queryBuilder.withSort(s -> s.field(f -> f.field("create_time").order(SortOrder.Desc)));

        NativeQuery searchQuery = queryBuilder.build();
        SearchHits<OrderDocument> searchHits = elasticsearchOperations.search(
                searchQuery, OrderDocument.class);

        List<OrderDocument> documents = searchHits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .collect(Collectors.toList());

        return mapper.toEntities(documents);
    }

    @Override
    public List<Order> findPayedOrders(LocalDateTime startTime, LocalDateTime endTime) {
        NativeQueryBuilder queryBuilder = new NativeQueryBuilder();
        BoolQuery.Builder boolBuilder = QueryBuilders.bool();

        Long startTimeMillis = toEpochMillis(startTime);
        Long endTimeMillis = toEpochMillis(endTime);

        if (startTimeMillis != null) {
            boolBuilder.must(QueryBuilders.range(ta -> ta.field("pay_time").gte(JsonData.of(startTimeMillis))));
        }

        if (endTimeMillis != null) {
            boolBuilder.must(QueryBuilders.range(ta -> ta.field("pay_time").lte(JsonData.of(endTimeMillis))));
        }

        boolBuilder.must(QueryBuilders.term(ta -> ta.field("is_payed").value(1L)));

        queryBuilder.withQuery(builder -> builder.bool(boolBuilder.build()));
        NativeQuery searchQuery = queryBuilder.build();

        SearchHits<OrderDocument> searchHits = elasticsearchOperations.search(
                searchQuery, OrderDocument.class);

        List<OrderDocument> documents = searchHits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .collect(Collectors.toList());

        return mapper.toEntities(documents);
    }

    @Override
    public Order save(Order order) {
        OrderDocument document = mapper.toDocument(order);
        OrderDocument savedDocument = elasticsearchRepository.save(document);
        return mapper.toEntity(savedDocument);
    }

    @Override
    public Order findById(Long orderId) {
        Optional<OrderDocument> document = elasticsearchRepository.findById(orderId);
        return document.map(mapper::toEntity).orElse(null);
    }

    @Override
    public void deleteById(Long orderId) {
        elasticsearchRepository.deleteById(orderId);
    }
}
