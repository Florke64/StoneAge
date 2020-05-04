/*
 * Copyright Go2Play.pl (c) 2020.
 * Program made for Go2Play Skyblock server. It's not allowed to re-distribute the code.
 * Author: FlrQue
 */

package win.flrque.g2p.stoneage.util;

import com.zaxxer.hikari.HikariConfig;
import org.bukkit.configuration.ConfigurationSection;

public class ConfigSectionDatabase extends ConfigSectionReader {

    private String serverAddress;
    private String serverPort;

    private String databaseName;
    private String username;
    private String password;

    private HikariConfig hikariConfig;

    public ConfigSectionDatabase(ConfigurationSection configurationSection) {
        super(configurationSection);
    }

    public void readDatabaseConnectionDetails() {
        serverAddress = rootSection.getString("server_address");
        serverPort = rootSection.getString("server_port");
        databaseName = rootSection.getString("db_name");
        username = rootSection.getString("db_user");
        password = rootSection.getString("db_password");

        //Creating Hikari Config
        hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl("jdbc:mysql://"+ serverAddress +":"+ serverPort +"/"+ databaseName);
        hikariConfig.setUsername(username);
        hikariConfig.setPassword(password);

        hikariConfig.setMaximumPoolSize(10);
    }

    public HikariConfig getHikariConfig() {
        return hikariConfig;
    }

    public String getServerAddress() {
        return serverAddress;
    }

    public String getServerPort() {
        return serverPort;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

}
