package net.mckitsu.lib.sqlite;

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
        System.out.println(table);

        table.getTable().put("uuid", "9a59d9ce-fdbe-4a68-8a3b-5aec7d559809");
        table.getTable().put("writeTime", "1234");

        System.out.println(sqLite.insert(table));

        System.out.println(sqLite.select("inventory", "9a59d9ce-fdbe-4a68-8a3b-5aec7d559809"));

    }
}
