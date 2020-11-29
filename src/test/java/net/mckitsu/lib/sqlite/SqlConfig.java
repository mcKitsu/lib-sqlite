package net.mckitsu.lib.sqlite;

import lombok.Data;

import java.util.Map;

@Data
public class SqlConfig {
    private String sql;
    private String tableName;
    private String primaryKey;
    private Map<String, String> table;

    public SQLiteTable toSQLiteTable(){
        SQLiteTable result = new SQLiteTable();
        result.setTableName(this.tableName);
        result.setPrimaryKey(this.primaryKey);
        result.setTable(this.table);
        return result;
    }
}
