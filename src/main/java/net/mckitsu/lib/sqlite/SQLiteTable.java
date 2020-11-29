package net.mckitsu.lib.sqlite;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class SQLiteTable {
    private String tableName;
    private String primaryKey;
    private Map<String, String> table;

    public SQLiteTable clone(){
        SQLiteTable result = new SQLiteTable();
        result.tableName = this.tableName;
        result.primaryKey = this.primaryKey;
        result.table = new HashMap<>(table);
        return result;
    }
}
