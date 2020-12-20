package net.mckitsu.lib.sqlite;

import net.mckitsu.lib.file.FileManager;
import net.mckitsu.lib.util.EventHandler;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class SQLite {
    /** 事件 **/
    public final Event event = new Event();
    private final Map<String, SQLiteTable> tableList = new HashMap<>();
    private final String filePath;
    /** 連線階段 **/
    private Connection connection;
    /** SQL 陳述語句 **/
    private Statement statement;
    private DatabaseMetaData databaseMetaData;

    /**
     * 使用檔案路徑
     * @param filePath 檔案路徑
     */
    public SQLite(String filePath){
        this.filePath = filePath;
    }

    /**
     * 使用檔案初始化
     * @param file 檔案
     */
    public SQLite(FileManager file){
        this.filePath = file.getDirPath() + '\\' + file.getFileName();
    }

    /**
     * 連線至SQLite檔案
     * @return 如果連線成功
     */
    public boolean connect(){
        if(this.connection != null){
            try {
                if(!this.connection.isClosed())
                    return false;
            } catch (SQLException ignored) {} //忽略Exception拋出的錯誤
        }

        try {
            this.connection = DriverManager.getConnection("jdbc:sqlite:" + this.filePath);
            this.statement = this.connection.createStatement();
            this.databaseMetaData = this.connection.getMetaData();
            this.loadTables();
            this.event.onConnect(this);
            return true;
        } catch (SQLException ignored) { //不使用Exception拋出的錯誤
            this.event.onConnectFail(this); //觸發onConnectFail的事件
            return false;
        }
    }

    /**
     * 取得所有資料表
     *
     * @return 資料表內容
     */
    public Map<String, SQLiteTable> getTables(){
        return new HashMap<>(this.tableList);
    }

    /**
     * 取得特定資料表
     *
     * @param tableName 資料表名稱
     * @return 資料表內容
     */
    public SQLiteTable getTable(String tableName){
        SQLiteTable result = this.tableList.get(tableName);

        if(result==null)
            return null;

        return result.clone();
    }

    /**
     * 確認資料表是否存在
     *
     * @param tableName 資料表名稱
     * @return 存在與否
     */
    public boolean tableIsExist(String tableName){
        return this.tableList.get(tableName) != null;
    }

    /**
     * 新增一個資料表
     *
     * @param table 資料表
     * @return 新增的結果
     * @see Status
     */
    public Status createTable(SQLiteTable table){
        if(this.tableIsExist(table.getTableName()))
            return Status.TABLE_IS_EXIST;

        String command = "CREATE TABLE \"%s\" (%s)";
        StringBuilder tables = new StringBuilder();
        tables.append(String.format("\"%s\" %s PRIMARY KEY NOT NULL",
                table.getPrimaryKey(),
                table.getTable().get(table.getPrimaryKey())));

        for(Map.Entry<String, String> entry : table.getTable().entrySet()){
            if(entry.getKey().equalsIgnoreCase(table.getPrimaryKey()))
                continue;

            tables.append(String.format(",%s %s", entry.getKey(), entry.getValue()));
        }

        String sql = String.format(command, table.getTableName(), tables.toString());

        try {
            this.statement.executeUpdate(sql);
            this.tableList.put(table.getTableName(), table);
            return Status.SUCCESS;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return Status.PARAM_ERROR;
        }
    }

    /**
     * 利用主鍵和資料表來查詢資料
     *
     * @param tableName 資料表名稱
     * @param primaryKey 主鍵
     * @return 利用該主鍵查出的資料
     */
    public SQLiteTable select(String tableName, String primaryKey){
        SQLiteTable result = this.tableList.get(tableName).clone();

        if(result == null){
            return null;
        }

        String command = "SELECT * FROM \"%s\" WHERE \"%s\" = \"%s\";";

        String sql = String.format(command, tableName, result.getPrimaryKey(), primaryKey);

        try {
            ResultSet resultSet = this.statement.executeQuery(sql);
            HashMap<String, String> data = new HashMap<>();
            for(Map.Entry<String, String> entry : result.getTable().entrySet())
                data.put(entry.getKey(), resultSet.getString(entry.getKey()));

            result.setTable(data);
            resultSet.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return null;
        }
        return result;
    }

    /**
     * 插入資料
     *
     * @param data 欲插入資料的資料表
     * @return 插入資料的結果
     * @see Status
     */
    public Status insert(SQLiteTable data){
        SQLiteTable sourceFormat = this.tableList.get(data.getTableName());
        if(sourceFormat == null)
            return Status.TABLE_IS_NOT_EXIST;

        String command = "INSERT OR REPLACE INTO \"%s\" (%s) VALUES (%s);";
        StringBuilder struct = new StringBuilder();
        StringBuilder value = new StringBuilder();

        for(Map.Entry<String, String> table : data.getTable().entrySet()){
            struct.append(table.getKey());
            struct.append(',');

            String typeName = sourceFormat.getTable().get(table.getKey());
            if(typeName == null)
                return Status.TABLE_FORMAT_NOT_MATCH;

            if(typeName.contains("CHAR")){
                value.append(String.format("\"%s\",", table.getValue()));
            }else if(typeName.contains("TEXT")){
                value.append(String.format("\"%s\",", table.getValue()));
            }else if(typeName.contains("INT")){
                try{
                    Integer.parseInt(table.getValue());
                }catch(NumberFormatException|NullPointerException  e) {
                    return Status.PARAM_ERROR;
                }
                value.append(String.format("%s,", table.getValue()));
            }
        }

        struct.deleteCharAt(struct.length()-1);
        value.deleteCharAt(value.length()-1);

        String sql = String.format(command, data.getTableName(), struct.toString(), value.toString());

        try {
            this.statement.executeUpdate(sql);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return Status.TRANSFER_ERROR;
        }
        return Status.SUCCESS;
    }

    /**
     * 關閉連線
     */
    public void close(){
        if(this.statement != null){
            try {
                this.statement.close();
                this.statement = null;
            } catch (SQLException|NullPointerException throwables) {
                throwables.printStackTrace();
            }
        }

        if(this.connection != null){
            try {
                this.connection.close();
                this.connection = null;
                event.onDisconnect(this);
            } catch (SQLException|NullPointerException ignored) {}
        }
    }

    /**
     * 取的資料庫中所有資料表
     */
    private void loadTables(){
        try {
            ResultSet tables = this.databaseMetaData.getTables(null, null, "", null);
            while(tables.next()){
                String tableName = tables.getString("TABLE_NAME");
                String primaryKeyName;
                Map<String, String> tableList = new HashMap<>();

                try {
                    ResultSet primaryKey = this.databaseMetaData.getPrimaryKeys(null, null, tableName);
                    primaryKeyName = primaryKey.getString("COLUMN_NAME");
                    primaryKey.close();
                }catch (SQLException throwables){
                    tables.close();
                    return;
                }

                try {
                    ResultSet columns = this.databaseMetaData.getColumns(null, null, tableName, null);
                    while (columns.next()){
                        tableList.put(columns.getString("COLUMN_NAME"), columns.getString("TYPE_NAME"));
                    }
                    columns.close();
                }catch (SQLException throwables){
                    tables.close();
                    return;
                }

                SQLiteTable table = new SQLiteTable();
                table.setTableName(tableName);
                table.setPrimaryKey(primaryKeyName);
                table.setTable(tableList);

                this.tableList.put(tableName, table);
            }
            tables.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    /* **************************************************************************************
     *  Class Event
     */
    /** 事件處理程序 **/
    public static class Event extends EventHandler{
        private Consumer<SQLite> onConnect;
        private Consumer<SQLite> onDisconnect;
        private Consumer<SQLite> onConnectFail;
        /**
         * 當連接時觸發此事件
         * @param sqLite 一個 SQLite 的物件**/
        private void onConnect(SQLite sqLite){
            super.execute(this.onConnect, sqLite);
        }
        /** 當連接失敗時觸發此事件
         * @param sqLite 一個 SQLite 的物件**/
        private void onConnectFail(SQLite sqLite){
            super.execute(this.onConnectFail, sqLite);
        }
        /** 當斷開連接時觸發此事件
         * @param sqLite 一個 SQLite 的物件**/
        private void onDisconnect(SQLite sqLite){
            super.execute(this.onDisconnect, sqLite);
        }
        /** 設定當連接時觸發該事件
         * @param onConnect Consumer型式的SQLite物件**/
        public void setOnConnect(Consumer<SQLite> onConnect) {
            this.onConnect = onConnect;
        }
        /** 設定當斷開連接時觸發該事件
         * @param onDisconnect Consumer型式的SQLite物件**/
        public void setOnDisconnect(Consumer<SQLite> onDisconnect) {
            this.onDisconnect = onDisconnect;
        }
        /** 設定當連接失敗時觸發該事件
         * @param onConnectFail Consumer型式的SQLite物件**/
        public void setOnConnectFail(Consumer<SQLite> onConnectFail) {
            this.onConnectFail = onConnectFail;
        }
    }

    /* **************************************************************************************
     *  Enum Status
     */

    /**
     * 狀態代碼
     */
    public enum Status{
        /** 成功 **/
        SUCCESS,
        /** 參數錯誤 **/
        PARAM_ERROR,
        /** 資料表存在 **/
        TABLE_IS_EXIST,
        /** 資料表不存在 **/
        TABLE_IS_NOT_EXIST,
        /** 資料表格式不相符 **/
        TABLE_FORMAT_NOT_MATCH,
        /** 傳送錯誤 **/
        TRANSFER_ERROR,
    }

}
