package com.mall4cloud.sync;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.cdc.connectors.mysql.source.MySqlSource;
import org.apache.flink.cdc.connectors.mysql.table.StartupOptions;
import org.apache.flink.configuration.*;
import org.apache.flink.core.execution.CheckpointingMode;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;

public class MySqlToEsSync {

    private static final Logger log = LoggerFactory.getLogger(MySqlToEsSync.class);

    public static void main(String[] args) throws Exception {

        initLogDir();

        // 创建Flink执行环境
        Configuration conf = new Configuration();
        conf.set(RestOptions.BIND_PORT, "8081");
        conf.set(WebOptions.LOG_PATH, "./logs/flink-job-manager.log");
        conf.set(TaskManagerOptions.TASK_MANAGER_LOG_PATH, "./logs/flink-job-manager.log");

        try (StreamExecutionEnvironment env = StreamExecutionEnvironment.createLocalEnvironmentWithWebUI(conf)) {
            // 配置检查点，确保数据一致性和故障恢复
            env.enableCheckpointing(60000); // 每60秒创建一个检查点
            env.getCheckpointConfig().setCheckpointingConsistencyMode(CheckpointingMode.EXACTLY_ONCE);
            env.getCheckpointConfig().setMinPauseBetweenCheckpoints(30000); // 两次检查点之间的最小时间间隔
            env.getCheckpointConfig().setCheckpointTimeout(60000); // 检查点超时时间
            env.getCheckpointConfig().setMaxConcurrentCheckpoints(1); // 同时只允许一个检查点
            env.getCheckpointConfig().setTolerableCheckpointFailureNumber(3); // 允许的检查点失败次数

            // 配置MySQL CDC Source
            MySqlSource<ChangedData> mySqlSource = buildDataChangeSource();

            // 添加CDC Source到环境
            DataStreamSource<ChangedData> stream = env.fromSource(mySqlSource,
                    WatermarkStrategy.noWatermarks(), "MySQL CDC Source");

            stream.setParallelism(4);

            // 配置Elasticsearch Sink
            ElasticsearchSink elasticsearchSink = new ElasticsearchSink();

            // 添加sink到流
            stream.sinkTo(elasticsearchSink).setParallelism(4);

            // 执行任务
            log.info("Starting Flink job: MySQL to Elasticsearch Sync");
            env.execute("MySQL to Elasticsearch Sync");
        } catch (Exception e) {
            log.error("Error executing Flink job", e);
            throw e; // 重新抛出异常，确保进程以非零状态退出
        }
    }

    private static void initLogDir() throws IOException {
        Path path = Paths.get("./logs");
        if (Files.exists(path) && Files.isDirectory(path)) {
            return;
        }
        if (Files.isRegularFile(path)) {
            log.error("logs directory exists and is not a directory");
            throw new RuntimeException("logs directory exists and is not a directory");
        }
        Files.createDirectories(path);
    }

    private static MySqlSource<ChangedData> buildDataChangeSource() {
        return MySqlSource.<ChangedData>builder()
                .hostname("127.0.0.1")
                .port(3306)
                .databaseList("mall4cloud_order")
                .tableList("mall4cloud_order.order", "mall4cloud_order.order_addr", "mall4cloud_order.order_item",
                        "mall4cloud_order.undo_log")
                .username("cluster")
                .password("cluster")
                .deserializer(new MysqlDeserialization())
                .serverTimeZone("UTC")
                .serverId("5500-5600") // 使用范围，避免冲突
                .startupOptions(StartupOptions.initial())
                .heartbeatInterval(Duration.ofSeconds(30000)) // 添加心跳间隔，保持连接活跃
                .build();
    }
}
