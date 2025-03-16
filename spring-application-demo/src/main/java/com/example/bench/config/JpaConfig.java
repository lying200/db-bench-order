package com.example.bench.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * JPA configuration
 * Uses Spring Boot's auto-configuration for JPA
 * The connection properties are defined in application.yml
 * Only active when app.repository.type=jpa
 */
@Configuration
@ConditionalOnProperty(name = "app.repository.type", havingValue = "jpa")
@EnableJpaRepositories(basePackages = {
        "com.example.bench.infrastructure.jpa.repository"
})
public class JpaConfig {

}
