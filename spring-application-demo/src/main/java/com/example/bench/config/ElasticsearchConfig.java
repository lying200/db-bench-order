package com.example.bench.config;

import jakarta.annotation.Nonnull;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.DefaultConnectionKeepAliveStrategy;
import org.apache.http.ssl.SSLContexts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.elasticsearch.ElasticsearchProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchClients;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

import javax.net.ssl.SSLContext;
import java.time.Duration;

/**
 * Elasticsearch configuration
 * Uses Spring Boot's auto-configuration for Elasticsearch client
 * The connection properties are defined in application.yml
 * Only active when app.repository.type=elasticsearch
 */
@Configuration
@ConditionalOnProperty(name = "app.repository.type", havingValue = "elasticsearch")
@EnableElasticsearchRepositories(basePackages = "com.example.bench.infrastructure.elasticsearch.repository")
public class ElasticsearchConfig extends ElasticsearchConfiguration {

    @Autowired
    private ElasticsearchProperties elasticsearchProperties;

    @Override
    @Nonnull
    public ClientConfiguration clientConfiguration() {
        try {
            BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(AuthScope.ANY,
                                               new UsernamePasswordCredentials(elasticsearchProperties.getUsername(),
                                                                               elasticsearchProperties.getPassword()));
            SSLContext sslContext = SSLContexts.custom()
                    .loadTrustMaterial(null, (chain, authType) -> true)
                    .build();
            return ClientConfiguration.builder()
                    .connectedTo(elasticsearchProperties.getUris().toArray(new String[0]))
                    .usingSsl(sslContext)
                    .withBasicAuth(elasticsearchProperties.getUsername(), elasticsearchProperties.getPassword())
                    .withConnectTimeout(Duration.ofSeconds(5))
                    .withSocketTimeout(Duration.ofSeconds(6))
                    .withClientConfigurer(
                            ElasticsearchClients.ElasticsearchHttpClientConfigurationCallback.from(clientBuilder -> {
                                clientBuilder.setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE);
                                clientBuilder.setKeepAliveStrategy(DefaultConnectionKeepAliveStrategy.INSTANCE);
                                return clientBuilder;
                            }))
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create Elasticsearch client", e);
        }
    }
}
