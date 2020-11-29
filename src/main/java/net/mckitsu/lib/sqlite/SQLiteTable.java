package net.mckitsu.lib.sqlite;

import lombok.Data;

import java.util.Map;

@Data
public class SQLiteTable {
    private String tableName;
    private String primaryKey;
    private Map<String, String> table;
}
