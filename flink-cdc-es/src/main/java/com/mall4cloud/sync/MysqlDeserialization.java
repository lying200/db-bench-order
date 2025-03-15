package com.mall4cloud.sync;


import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.cdc.connectors.shaded.org.apache.kafka.connect.data.Struct;
import org.apache.flink.cdc.connectors.shaded.org.apache.kafka.connect.source.SourceRecord;
import org.apache.flink.cdc.debezium.DebeziumDeserializationSchema;
import org.apache.flink.util.Collector;

import java.io.Serial;

public class MysqlDeserialization implements DebeziumDeserializationSchema<ChangedData> {

    @Serial
    private static final long serialVersionUID = 2339470382519089901L;

    @Override
    public void deserialize(SourceRecord sourceRecord, Collector<ChangedData> collector) throws Exception {
        Struct struct = (Struct) sourceRecord.value();
        ChangedData changedData = new CdcJsonToChangedDataConverter().map(struct);
        collector.collect(changedData);
    }

    @Override
    public TypeInformation<ChangedData> getProducedType() {
        return TypeInformation.of(ChangedData.class);
    }
}
