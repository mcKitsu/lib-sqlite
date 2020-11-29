package net.mckitsu.lib.sqlite;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import net.mckitsu.lib.util.EventHandler;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class SQLite {
    public final Event event = new Event();

    private final Map<String, SQLiteTable> tableList = new HashMap<>();
    private final String filePath;

    private Connection connection;
    private Statement statement;
    private DatabaseMetaData databaseMetaData;

    /* **************************************************************************************
     *  Construct method
     */
    public SQLite(String filePath){
        this.filePath = filePath;
    }
    /* **************************************************************************************
     *  Override method
     */

    /* **************************************************************************************
     *  Public method
     */
    public boolean connect(){
        if(this.connection != null){
            try {
                if(!this.connection.isClosed())
                    return false;
            } catch (SQLException ignored) {}
        }

        try {
            this.connection = DriverManager.getConnection("jdbc:sqlite:" + this.filePath);
            this.statement = this.connection.createStatement();
            this.databaseMetaData = this.connection.getMetaData();
            this.loadTables();
            this.event.onConnect(this);
            return true;
        } catch (SQLException ignore) {
            this.event.onConnectFail(this);
            return false;
        }
    }

    public Map<String, SQLiteTable> getTables(){
        return this.tableList;
    }

    public SQLiteTable getTable(String tableName){
        return this.tableList.get(tableName);
    }

    public boolean tableIsExist(String tableName){
        return this.tableList.get(tableName) != null;
    }

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

        System.out.println(sql);

        try {
            this.statement.executeUpdate(sql);
            this.tableList.put(table.getTableName(), table);
            return Status.SUCCESS;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return Status.PARAM_ERROR;
        }
    }

    public Status insert(SQLiteTable data){
        SQLiteTable sourceFormat = this.tableList.get(data.getTableName());
        if(sourceFormat == null)
            return Status.TABLE_IS_NOT_EXIST;

        String command = "INSERT INTO STRUCT (%s) VALUES (%s);";
        StringBuilder struct = new StringBuilder();
        StringBuilder value = new StringBuilder();

        for(Map.Entry<String, String> table : data.getTable().entrySet()){
            struct.append(table.getKey());
            struct.append(',');

            String typeName = sourceFormat.getTable().get(table.getKey());
            if(typeName == null)
                return Status.TABLE_FORMAT_NOT_MATCH;

            //if(typeName.contains("CHAR")){
                value.append(String.format("\"%s\",", table.getValue()));
            //}
        }

        struct.deleteCharAt(struct.length()-1);
        value.deleteCharAt(value.length()-1);

        System.out.println(struct.toString());
        System.out.println(value.toString());
        return Status.SUCCESS;
    }

    public void close(){
        try {
            this.statement.close();
        } catch (SQLException|NullPointerException throwables) {
            throwables.printStackTrace();
        }

        try {
            this.connection.close();
        } catch (SQLException|NullPointerException ignored) {}
    }

    /* **************************************************************************************
     *  Protected method
     */

    /* **************************************************************************************
     *  Private method
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
    @Setter
    public static class Event extends EventHandler{
        private Consumer<SQLite> onConnect;
        private Consumer<SQLite> onDisconnect;
        private Consumer<SQLite> onConnectFail;

        private void onConnect(SQLite sqLite){
            super.execute(this.onConnect, sqLite);
        }

        private void onConnectFail(SQLite sqLite){
            super.execute(this.onConnectFail, sqLite);
        }

        private void onDisconnect(SQLite sqLite){
            super.execute(this.onDisconnect, sqLite);
        }
    }

    /* **************************************************************************************
     *  Enum Status
     */
    public enum Status{
        SUCCESS,
        PARAM_ERROR,
        TABLE_IS_EXIST,
        TABLE_IS_NOT_EXIST,
        TABLE_FORMAT_NOT_MATCH,
    }

}
