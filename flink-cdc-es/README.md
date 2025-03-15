# flink cdc mysql to elasticsearch

将数据从 Mysql 同步至 ElasticSearch

## 启动同步

+ 修改 `com.mall4cloud.sync.MySqlToEsSync` 其中的 `Mysql` 连接配置;
+ 修改 `com.mall4cloud.sync.ElasticsearchSinkWriter.ElasticsearchSinkWriter` 中 `ElasticSearch` 的连接配置;
+ 启动类： `com.mall4cloud.sync.MySqlToEsSync`;

## 关于启动后数据不同步问题

+ 该问题为 `flink cdc` 的 bug;
+ 由于该问题已修复，但尚未包含在 `flink cdc 3.3.0` 版本中，因此该项目拷贝修复后的对应代码文件，位置为：
  `org/apache/flink/cdc/connectors/mysql`
+ 相关issue和pr:
    + https://issues.apache.org/jira/browse/FLINK-37313
    + https://issues.apache.org/jira/browse/FLINK-37191
    + https://github.com/apache/flink-cdc/pull/3902