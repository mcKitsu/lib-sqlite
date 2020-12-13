package net.mckitsu.lib.sqlite;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class SQLiteTable {
    /** 資料表名稱 **/
    private String tableName;
    /** 資料表主鍵 **/
    private String primaryKey;
    /** 資料表資料 **/
    private Map<String, String> table;

    /** 複製資料表 **/
    public SQLiteTable clone(){
        SQLiteTable result = new SQLiteTable();
        result.tableName = this.tableName;
        result.primaryKey = this.primaryKey;
        result.table = new HashMap<>(table);
        return result;
    }
}
