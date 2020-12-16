package net.mckitsu.lib.sqlite;

import java.util.HashMap;
import java.util.Map;

public class SQLiteTable {
    /** 資料表名稱 **/
    private String tableName;
    /** 資料表主鍵 **/
    private String primaryKey;
    /** 資料表資料 **/
    private Map<String, String> table;

    public SQLiteTable() {
    }

    /** 複製資料表 **/
    public SQLiteTable clone(){
        SQLiteTable result = new SQLiteTable();
        result.tableName = this.tableName;
        result.primaryKey = this.primaryKey;
        result.table = new HashMap<>(table);
        return result;
    }

    public String getTableName() {
        return this.tableName;
    }

    public String getPrimaryKey() {
        return this.primaryKey;
    }

    public Map<String, String> getTable() {
        return this.table;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public void setPrimaryKey(String primaryKey) {
        this.primaryKey = primaryKey;
    }

    public void setTable(Map<String, String> table) {
        this.table = table;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof SQLiteTable)) return false;
        final SQLiteTable other = (SQLiteTable) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$tableName = this.getTableName();
        final Object other$tableName = other.getTableName();
        if (this$tableName == null ? other$tableName != null : !this$tableName.equals(other$tableName)) return false;
        final Object this$primaryKey = this.getPrimaryKey();
        final Object other$primaryKey = other.getPrimaryKey();
        if (this$primaryKey == null ? other$primaryKey != null : !this$primaryKey.equals(other$primaryKey))
            return false;
        final Object this$table = this.getTable();
        final Object other$table = other.getTable();
        if (this$table == null ? other$table != null : !this$table.equals(other$table)) return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof SQLiteTable;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $tableName = this.getTableName();
        result = result * PRIME + ($tableName == null ? 43 : $tableName.hashCode());
        final Object $primaryKey = this.getPrimaryKey();
        result = result * PRIME + ($primaryKey == null ? 43 : $primaryKey.hashCode());
        final Object $table = this.getTable();
        result = result * PRIME + ($table == null ? 43 : $table.hashCode());
        return result;
    }

    public String toString() {
        return "SQLiteTable(tableName=" + this.getTableName() + ", primaryKey=" + this.getPrimaryKey() + ", table=" + this.getTable() + ")";
    }
}
