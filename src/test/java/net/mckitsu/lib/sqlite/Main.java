package net.mckitsu.lib.sqlite;

import java.util.Map;

public class Main {
    public static void main(String[] args){
        SQLite sqLite = new SQLite("database.db");
        boolean result = sqLite.connect();
        SqlConfigManager sqlConfigManager = new SqlConfigManager("config");
        sqlConfigManager.loadConfig();

        for(SqlConfig config : sqlConfigManager.getConfigs()){
            sqLite.createTable(config.toSQLiteTable());
        }

        SQLiteTable table = sqLite.getTable("inventory");
        table.getTable().put("uuid", "9a59d9ce-fdbe-4a68-8a3b-5aec7d559809");

        sqLite.insert(table);

    }
}
