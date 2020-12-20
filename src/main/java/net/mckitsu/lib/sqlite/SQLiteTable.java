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
    /** 取得資料表名稱
     * @return 資料表名稱**/
    public String getTableName() {
        return this.tableName;
    }
    /** 取得資料表主鍵
     * @return 主鍵的值**/
    public String getPrimaryKey() {
        return this.primaryKey;
    }
    /** 取得資料表
     * @return 資料表**/
    public Map<String, String> getTable() {
        return this.table;
    }
    /** 設定資料表名稱
     * @param tableName 資料表名稱
     * **/
    public void setTableName(String tableName) {
        this.tableName = tableName;
    }
    /** 設定資料表主鍵
     * @param primaryKey 主鍵**/
    public void setPrimaryKey(String primaryKey) {
        this.primaryKey = primaryKey;
    }
    /** 設定資料表
     * @param table 資料表**/
    public void setTable(Map<String, String> table) {
        this.table = table;
    }
    /** 判斷兩資料表是否相同 **/
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
    /** 判斷兩Object是否能被比較
     * @param other 其他的Object
     * @return 確認後的結果**/
    protected boolean canEqual(final Object other) {
        return other instanceof SQLiteTable;
    }
    /** 取得哈希值 **/
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
    /** 把資料表轉換成文字 **/
    public String toString() {
        return "SQLiteTable(tableName=" + this.getTableName() + ", primaryKey=" + this.getPrimaryKey() + ", table=" + this.getTable() + ")";
    }
}
