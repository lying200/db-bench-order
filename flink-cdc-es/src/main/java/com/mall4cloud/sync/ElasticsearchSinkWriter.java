package com.mall4cloud.sync;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.bulk.BulkResponseItem;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.flink.api.common.serialization.BulkWriter;
import org.apache.flink.api.connector.sink2.SinkWriter;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.DefaultConnectionKeepAliveStrategy;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.ssl.SSLContexts;
import org.elasticsearch.client.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ElasticsearchSinkWriter implements SinkWriter<ChangedData> {
    private static final Logger LOG = LoggerFactory.getLogger(ElasticsearchSinkWriter.class);

    private final ElasticsearchClient client;
    private final BulkWriter<ChangedData> bulkWriter;
    private static final int MAX_RETRY_COUNT = 5;
    private static final Duration RETRY_BACKOFF = Duration.ofSeconds(5);

    public ElasticsearchSinkWriter() {
        // 创建 Elasticsearch 客户端
        this.client = createClient();
        this.bulkWriter = createBulkWriter();
        LOG.info("ElasticsearchSinkWriter initialized successfully");
    }

    private ElasticsearchClient createClient() {
        // 从环境变量或配置文件中获取连接信息，如果没有则使用默认值
        String serverUrl = System.getProperty("es.url", "https://localhost:9200");
        String username = System.getProperty("es.username", "elastic");
        String password = System.getProperty("es.password", "elastic");
        int connectionTimeout = Integer.parseInt(System.getProperty("es.connectionTimeout", "5000"));
        int socketTimeout = Integer.parseInt(System.getProperty("es.socketTimeout", "60000"));

        LOG.info("Connecting to Elasticsearch at {} with user {}", serverUrl, username);

        BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));

        try {
            SSLContext sslContext = SSLContexts.custom()
                    .loadTrustMaterial(null, (chain, authType) -> true)
                    .build();
            
            RestClientBuilder restClientBuilder = RestClient.builder(HttpHost.create(serverUrl))
                .setHttpClientConfigCallback(httpClientBuilder -> {
                    httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
                    httpClientBuilder.setSSLContext(sslContext);
                    httpClientBuilder.setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE);
                    httpClientBuilder.setKeepAliveStrategy(DefaultConnectionKeepAliveStrategy.INSTANCE);
                    httpClientBuilder.setMaxConnTotal(50);
                    httpClientBuilder.setMaxConnPerRoute(10);
                    return customizeHttpClient(httpClientBuilder);
                })
                .setRequestConfigCallback(requestConfigBuilder -> {
                    requestConfigBuilder.setConnectTimeout(connectionTimeout);
                    requestConfigBuilder.setSocketTimeout(socketTimeout);
                    return requestConfigBuilder;
                });

            RestClient restClient = restClientBuilder.build();
            
            // 测试连接
            testConnection(restClient);
            
            ElasticsearchTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());
            return new ElasticsearchClient(transport);
        } catch (Exception e) {
            LOG.error("Failed to create Elasticsearch client", e);
            throw new RuntimeException("Failed to create Elasticsearch client", e);
        }
    }
    
    private HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpClientBuilder) {
        return httpClientBuilder;
    }
    
    private void testConnection(RestClient restClient) {
        int retries = 0;
        boolean connected = false;
        Exception lastException = null;
        
        while (!connected && retries < MAX_RETRY_COUNT) {
            try {
                Response response = restClient.performRequest(new Request("GET", "/_cluster/health"));
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode >= 200 && statusCode < 300) {
                    LOG.info("Successfully connected to Elasticsearch cluster");
                    connected = true;
                } else {
                    LOG.warn("Received status code {} from Elasticsearch", statusCode);
                    retries++;
                    sleepBeforeRetry(retries);
                }
            } catch (Exception e) {
                lastException = e;
                LOG.warn("Failed to connect to Elasticsearch (attempt {}/{}): {}", 
                         retries + 1, MAX_RETRY_COUNT, e.getMessage());
                retries++;
                sleepBeforeRetry(retries);
            }
        }
        
        if (!connected) {
            LOG.error("Failed to connect to Elasticsearch after {} attempts", MAX_RETRY_COUNT);
            if (lastException != null) {
                throw new RuntimeException("Failed to connect to Elasticsearch", lastException);
            } else {
                throw new RuntimeException("Failed to connect to Elasticsearch");
            }
        }
    }
    
    private void sleepBeforeRetry(int retryCount) {
        try {
            long sleepTime = RETRY_BACKOFF.toMillis() * retryCount;
            LOG.info("Waiting {} ms before retry", sleepTime);
            TimeUnit.MILLISECONDS.sleep(sleepTime);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private BulkWriter<ChangedData> createBulkWriter() {
        return new ElasticSearchBulkWriter(client);
    }

    @Override
    public void write(ChangedData element, Context context) throws IOException, InterruptedException {
        try {
            LOG.debug("Processing element: {}", element);
            bulkWriter.addElement(element);
        } catch (Exception e) {
            LOG.error("Error processing element: {}", element, e);
            throw new IOException("Failed to process element", e);
        }
    }

    @Override
    public void flush(boolean endOfInput) throws IOException, InterruptedException {
        LOG.info("Flushing bulk processor...");
        try {
            bulkWriter.flush();
        } catch (Exception e) {
            LOG.error("Error flushing bulk processor", e);
            throw new IOException("Failed to flush bulk processor", e);
        }
    }

    @Override
    public void close() throws Exception {
        LOG.info("Closing Elasticsearch sink writer...");
        try {
            if (bulkWriter != null) {
                bulkWriter.finish();
            }
            if (client != null) {
                client.close();
            }
        } catch (Exception e) {
            LOG.error("Error closing resources", e);
            throw e;
        }
    }
}

class ElasticSearchBulkWriter implements BulkWriter<ChangedData> {
    private static final Logger LOG = LoggerFactory.getLogger(ElasticSearchBulkWriter.class);
    private static final int MAX_BULK_ACTIONS = 1000;
    private static final int MAX_RETRY_COUNT = 3;
    private static final long RETRY_BACKOFF_MS = 1000;

    private final ElasticsearchClient client;
    private final List<BulkOperation> operations;
    private long lastFlushTime;
    private static final long FLUSH_INTERVAL_MS = 10000; // 10秒自动刷新

    public ElasticSearchBulkWriter(ElasticsearchClient client) {
        this.client = client;
        this.operations = new ArrayList<>();
        this.lastFlushTime = System.currentTimeMillis();
        LOG.info("ElasticSearchBulkWriter initialized with batch size: {}", MAX_BULK_ACTIONS);
    }

    @Override
    public void addElement(ChangedData element) throws IOException {
        BulkOperation operation = getBulkOperation(element);

        synchronized (operations) {
            operations.add(operation);
            LOG.debug("Added operation to batch. Current size: {}", operations.size());

            // 如果达到批量处理阈值或者距离上次刷新已经过了指定时间，执行刷新
            long currentTime = System.currentTimeMillis();
            if (operations.size() >= MAX_BULK_ACTIONS || (currentTime - lastFlushTime) > FLUSH_INTERVAL_MS) {
                doFlush();
                lastFlushTime = currentTime;
            }
        }
    }

    private static BulkOperation getBulkOperation(ChangedData element) {
        BulkOperation operation;

        if ("d".equals(element.getOperation())) {
            // 处理删除操作
            operation = new BulkOperation(
                    BulkOperationType.DELETE,
                    element.getTable(),
                    element.getPrimaryKeyValue(),
                    null
            );
        } else {
            // 处理创建或更新操作
            operation = new BulkOperation(
                    BulkOperationType.INDEX,
                    element.getTable(),
                    element.getPrimaryKeyValue(),
                    element.getData()
            );
        }
        return operation;
    }

    @Override
    public void flush() throws IOException {
        synchronized (operations) {
            if (!operations.isEmpty()) {
                doFlush();
                lastFlushTime = System.currentTimeMillis();
            }
        }
    }

    private void doFlush() throws IOException {
        if (operations.isEmpty()) {
            return;
        }

        int retryCount = 0;
        boolean success = false;
        int operationCount = operations.size();
        
        LOG.info("Executing bulk request with {} operations", operationCount);

        while (!success && retryCount < MAX_RETRY_COUNT) {
            try {
                BulkRequest bulkRequest = getBulkRequest();
                BulkResponse response = client.bulk(bulkRequest);

                // 处理响应
                if (response.errors()) {
                    // 有错误发生
                    int errorCount = 0;
                    for (BulkResponseItem item : response.items()) {
                        if (item.error() != null) {
                            errorCount++;
                            LOG.error("Failed to process item {}: {}",
                                    item.id(), item.error().reason());
                        }
                    }
                    
                    if (errorCount > 0) {
                        LOG.warn("{} out of {} operations failed", errorCount, operationCount);
                    }
                }

                LOG.info("Bulk request completed with {} items in {} ms",
                        operationCount, response.took());
                
                success = true;
                // 清空操作列表
                operations.clear();

            } catch (Exception e) {
                retryCount++;
                
                if (retryCount < MAX_RETRY_COUNT) {
                    LOG.warn("Error executing bulk request (attempt {}/{}): {}. Retrying...", 
                             retryCount, MAX_RETRY_COUNT, e.getMessage());
                    try {
                        Thread.sleep(RETRY_BACKOFF_MS * retryCount);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new IOException("Interrupted during retry backoff", ie);
                    }
                } else {
                    LOG.error("Error executing bulk request after {} attempts", MAX_RETRY_COUNT, e);
                }
            }
        }
        
        if (!success) {
            LOG.error("Failed to execute bulk request after {} attempts", MAX_RETRY_COUNT);
        }
    }

    private BulkRequest getBulkRequest() {
        BulkRequest.Builder bulkRequestBuilder = new BulkRequest.Builder();

        for (BulkOperation operation : operations) {
            switch (operation.type) {
                case INDEX:
                    bulkRequestBuilder.operations(op -> op
                            .index(idx -> idx
                                    .index(operation.index)
                                    .id(operation.id)
                                    .document(operation.data)
                            )
                    );
                    break;
                case DELETE:
                    bulkRequestBuilder.operations(op -> op
                            .delete(del -> del
                                    .index(operation.index)
                                    .id(operation.id)
                            )
                    );
                    break;
            }
        }

        return bulkRequestBuilder.build();
    }

    @Override
    public void finish() throws IOException {
        flush();
    }

    // 内部类用于表示批量操作
    private record BulkOperation(BulkOperationType type, String index, String id, Object data) {
    }

    // 枚举用于表示操作类型
    private enum BulkOperationType {
        INDEX, DELETE
    }
}
