package com.example.bench.infrastructure.jpa;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class to conditionally scan JPA components
 * Only active when app.repository.type=jpa
 */
@Configuration
@ConditionalOnProperty(name = "app.repository.type", havingValue = "jpa")
@ComponentScan(basePackages = {
        "com.example.bench.infrastructure.jpa.gateway",
        "com.example.bench.infrastructure.jpa.repository"
})
public class JpaComponentScanConfig {
}
