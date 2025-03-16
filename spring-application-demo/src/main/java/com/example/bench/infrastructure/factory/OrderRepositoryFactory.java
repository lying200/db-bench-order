package com.example.bench.infrastructure.factory;

import com.example.bench.domain.repository.OrderRepositoryGateway;
import com.example.bench.infrastructure.elasticsearch.gateway.OrderElasticsearchGateway;
import com.example.bench.infrastructure.jpa.gateway.OrderJpaGateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Factory for selecting the appropriate OrderRepositoryGateway implementation
 */
@Component
public class OrderRepositoryFactory {

    private final OrderJpaGateway jpaGateway;
    private final OrderElasticsearchGateway elasticsearchGateway;

    public OrderRepositoryFactory(@Autowired(required = false) OrderJpaGateway jpaGateway,
                                  @Autowired(required = false) OrderElasticsearchGateway elasticsearchGateway) {
        this.jpaGateway = jpaGateway;
        this.elasticsearchGateway = elasticsearchGateway;
    }

    @Value("${app.repository.type:jpa}")
    private String repositoryType;

    /**
     * Get the appropriate repository implementation based on configuration
     *
     * @return OrderRepositoryGateway implementation
     */
    public OrderRepositoryGateway getOrderRepository() {
        return switch (repositoryType.toLowerCase()) {
            case "elasticsearch" -> elasticsearchGateway;
            default -> jpaGateway;
        };
    }
}
