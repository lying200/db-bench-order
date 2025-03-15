package com.mall4cloud.sync;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.cdc.connectors.shaded.org.apache.kafka.connect.data.Field;
import org.apache.flink.cdc.connectors.shaded.org.apache.kafka.connect.data.Schema;
import org.apache.flink.cdc.connectors.shaded.org.apache.kafka.connect.data.Struct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 将 Debezium JSON 格式转换为 ChangedData 对象
 */
public class CdcJsonToChangedDataConverter implements MapFunction<Struct, ChangedData> {
    private static final Logger LOG = LoggerFactory.getLogger(CdcJsonToChangedDataConverter.class);

    public static final String TS_MS = "ts_ms";
    public static final String BEFORE = "before";
    public static final String AFTER = "after";
    public static final String SOURCE = "source";

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * 获取操作类型
     * <a href="https://debezium.io/documentation/reference/stable/connectors/mysql.html#mysql-create-events">debezium事件说明</a>
     */
    private static final Map<String, Integer> OPERATION_MAP = Map.of(
            "c", 0,
            "u", 1,
            "d", 2,
            "r", 3);

    @Override
    public ChangedData map(Struct struct) throws Exception {
        try {
            final Struct source = struct.getStruct(SOURCE);
            final String database = source.getString("db");
            final String table = source.getString("table");
            // 确定主键（这里假设主键名为 "id"，实际应用中可能需要动态确定）
            String primaryKeyName = determinePrimaryKey(table);

            // 获取操作类型
            String op = struct.getString("op");


            Map<String, Object> data = switch (op) {
                case "c", "u", "r" -> structToMap(struct.getStruct(AFTER));
                case "d" -> structToMap(struct.getStruct(BEFORE));
                default -> throw new IllegalArgumentException("Unsupported operation: " + op);
            };

            String primaryKeyValue = data.get(primaryKeyName).toString();


            // 创建 ChangedData 对象
            return new ChangedData(op, database, table, primaryKeyName, primaryKeyValue, data);
        } catch (Exception e) {
            LOG.error("Error converting JSON to ChangedData: {}", struct, e);
            throw e;
        }
    }

    /**
     * 将 JsonNode 转换为 Map
     */
    private Map<String, Object> structToMap(Struct struct) {
        Map<String, Object> map = new HashMap<>();

        Schema schema = struct.schema();
        List<Field> fields = schema.fields();

        for (Field field : fields) {
            String name = field.name();
            Object value = struct.get(name);
            map.put(name, value);
        }

        return map;
    }

    /**
     * 根据表名确定主键名
     */
    private String determinePrimaryKey(String table) {
        // 这里根据表名返回对应的主键名
        return switch (table) {
            case "order" -> "order_id";
            case "order_addr" -> "order_addr_id";
            case "order_item" -> "order_item_id";
            case "undo_log" -> "id";
            default -> "id";
        };
    }
}
