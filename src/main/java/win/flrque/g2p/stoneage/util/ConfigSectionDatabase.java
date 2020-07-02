/*
 * Copyright Go2Play.pl (c) 2020.
 * Program made for Go2Play Skyblock server. It's not allowed to re-distribute the code.
 * Author: FlrQue
 */

package win.flrque.g2p.stoneage.util;

import org.bukkit.configuration.ConfigurationSection;

public class ConfigSectionDatabase extends ConfigSectionReader {

    private String serverAddress;
    private int serverPort;

    private String databaseName;
    private String username;
    private String password;

    public ConfigSectionDatabase(ConfigurationSection configurationSection) {
        super(configurationSection);
    }

    public void readDatabaseConnectionDetails() {
        serverAddress = rootSection.getString("server_address");
        serverPort = rootSection.getInt("server_port");
        databaseName = rootSection.getString("db_name");
        username = rootSection.getString("db_user");
        password = rootSection.getString("db_password");
    }

    public String getServerAddress() {
        return serverAddress;
    }

    public int getServerPort() {
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
