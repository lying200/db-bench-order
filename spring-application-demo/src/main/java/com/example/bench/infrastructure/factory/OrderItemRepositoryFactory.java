package com.example.bench.infrastructure.factory;

import com.example.bench.domain.repository.OrderItemRepositoryGateway;
import com.example.bench.infrastructure.elasticsearch.gateway.OrderItemElasticsearchGateway;
import com.example.bench.infrastructure.jpa.gateway.OrderItemJpaGateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Factory for selecting the appropriate OrderItemRepositoryGateway implementation
 */
@Component
public class OrderItemRepositoryFactory {

    private final OrderItemJpaGateway jpaGateway;
    private final OrderItemElasticsearchGateway elasticsearchGateway;

    public OrderItemRepositoryFactory(@Autowired(required = false) OrderItemJpaGateway jpaGateway,
                                      @Autowired(required = false) OrderItemElasticsearchGateway elasticsearchGateway) {
        this.jpaGateway = jpaGateway;
        this.elasticsearchGateway = elasticsearchGateway;
    }

    @Value("${app.repository.type:jpa}")
    private String repositoryType;

    /**
     * Get the appropriate repository implementation based on configuration
     *
     * @return OrderItemRepositoryGateway implementation
     */
    public OrderItemRepositoryGateway getOrderItemRepository() {
        return switch (repositoryType.toLowerCase()) {
            case "elasticsearch" -> elasticsearchGateway;
            default -> jpaGateway;
        };
    }
}
