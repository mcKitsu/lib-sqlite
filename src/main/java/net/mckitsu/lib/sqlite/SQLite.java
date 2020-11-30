package net.mckitsu.lib.sqlite;

import lombok.Setter;
import net.mckitsu.lib.file.FileManager;
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

    public SQLite(FileManager file){
        this.filePath = file.getDirPath() + '\\' + file.getFileName();
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
        return new HashMap<>(this.tableList);
    }

    public SQLiteTable getTable(String tableName){
        SQLiteTable result = this.tableList.get(tableName);

        if(result==null)
            return null;

        return result.clone();
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

        try {
            this.statement.executeUpdate(sql);
            this.tableList.put(table.getTableName(), table);
            return Status.SUCCESS;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return Status.PARAM_ERROR;
        }
    }

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
        TRANSFER_ERROR,
    }

}
