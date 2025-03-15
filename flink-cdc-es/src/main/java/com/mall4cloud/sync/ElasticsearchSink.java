package com.mall4cloud.sync;

import org.apache.flink.api.connector.sink2.Sink;
import org.apache.flink.api.connector.sink2.SinkWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Properties;

public class ElasticsearchSink implements Sink<ChangedData> {
    private static final Logger LOG = LoggerFactory.getLogger(ElasticsearchSink.class);

    public ElasticsearchSink() {
        this(new Properties());
    }

    public ElasticsearchSink(Properties properties) {
    }

    @Override
    public SinkWriter<ChangedData> createWriter(InitContext context) throws IOException {
        LOG.info("Creating ElasticsearchSinkWriter");
        return new ElasticsearchSinkWriter();
    }
}
