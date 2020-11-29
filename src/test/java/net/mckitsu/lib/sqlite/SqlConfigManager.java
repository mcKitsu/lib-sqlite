package net.mckitsu.lib.sqlite;

import lombok.Getter;
import net.mckitsu.lib.file.FileManager;
import net.mckitsu.lib.file.FolderManager;
import net.mckitsu.lib.file.YamlManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SqlConfigManager extends FolderManager{
    private final @Getter List<SqlConfig> configs = new ArrayList<>();
    private final YamlManager<SqlConfig> yamlLoader = new YamlManager<>(SqlConfig.class);

    public SqlConfigManager(String dirPath) {
        super(dirPath);
        createDir();
    }

    public void loadConfig(){
        configs.clear();

        for(FileManager file : super.getFiles()){
            try {
                SqlConfig config = yamlLoader.load(file);
                configs.add(config);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
