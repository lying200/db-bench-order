package com.example.bench.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchDataAutoConfiguration;
import org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Repository auto-configuration that conditionally enables JPA or Elasticsearch
 * based on the app.repository.type property
 */
@Configuration
public class RepositoryAutoConfiguration {

    /**
     * Auto-configuration for JPA repositories
     * Only active when app.repository.type=jpa
     */
    @Configuration
    @ConditionalOnProperty(name = "app.repository.type", havingValue = "jpa")
    @Import({
            DataSourceAutoConfiguration.class,
            DataSourceTransactionManagerAutoConfiguration.class,
            HibernateJpaAutoConfiguration.class,
            JpaRepositoriesAutoConfiguration.class,
            JpaConfig.class
    })
    public static class JpaAutoConfig {
    }

    /**
     * Auto-configuration for Elasticsearch repositories
     * Only active when app.repository.type=elasticsearch
     */
    @Configuration
    @ConditionalOnProperty(name = "app.repository.type", havingValue = "elasticsearch")
    @Import({
            ElasticsearchDataAutoConfiguration.class,
            ElasticsearchRepositoriesAutoConfiguration.class,
            ElasticsearchConfig.class
    })
    public static class ElasticsearchAutoConfig {
    }
}
