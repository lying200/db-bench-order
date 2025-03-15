package com.mall4cloud.sync;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

public class ChangedData implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String operation; // c=create, u=update, d=delete, r=read
    private String database;
    private String table;
    private String primaryKeyName;
    private String primaryKeyValue;
    private Map<String, Object> data;

    public ChangedData() {
    }

    public ChangedData(String operation, String database, String table,
                       String primaryKeyName, String primaryKeyValue, Map<String, Object> data) {
        this.operation = operation;
        this.database = database;
        this.table = table;
        this.primaryKeyName = primaryKeyName;
        this.primaryKeyValue = primaryKeyValue;
        this.data = data;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public String getPrimaryKeyName() {
        return primaryKeyName;
    }

    public void setPrimaryKeyName(String primaryKeyName) {
        this.primaryKeyName = primaryKeyName;
    }

    public String getPrimaryKeyValue() {
        return primaryKeyValue;
    }

    public void setPrimaryKeyValue(String primaryKeyValue) {
        this.primaryKeyValue = primaryKeyValue;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "ChangedData{" +
                "operation='" + operation + '\'' +
                ", database='" + database + '\'' +
                ", table='" + table + '\'' +
                ", primaryKeyName='" + primaryKeyName + '\'' +
                ", primaryKeyValue='" + primaryKeyValue + '\'' +
                ", data=" + data +
                '}';
    }
}
