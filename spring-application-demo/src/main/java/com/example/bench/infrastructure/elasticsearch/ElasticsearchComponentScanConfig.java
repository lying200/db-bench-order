package com.example.bench.infrastructure.elasticsearch;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class to conditionally scan Elasticsearch components
 * Only active when app.repository.type=elasticsearch
 */
@Configuration
@ConditionalOnProperty(name = "app.repository.type", havingValue = "elasticsearch")
@ComponentScan(basePackages = {
        "com.example.bench.infrastructure.elasticsearch.gateway",
        "com.example.bench.infrastructure.elasticsearch.repository"
})
public class ElasticsearchComponentScanConfig {
}
