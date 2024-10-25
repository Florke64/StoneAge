/*
 * @Florke64 <Daniel Chojnacki>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package pl.florke.stoneage.config;

import org.bukkit.configuration.ConfigurationSection;

public class DatabaseConfigReader extends ConfigSectionReader {

    private String serverAddress;
    private int serverPort;

    private String databaseName;
    private String username;
    private String password;

    private boolean security;

    public DatabaseConfigReader(ConfigurationSection configurationSection) {
        super(configurationSection);
    }

    public void readDatabaseConnectionDetails() {
        serverAddress = rootSection.getString("server_address", "localhost");
        serverPort = rootSection.getInt("server_port", 3306);
        databaseName = rootSection.getString("db_name", "stoneage");
        username = rootSection.getString("db_user", "root");
        password = rootSection.getString("db_password");
        security = rootSection.getBoolean("db_ssl", true);
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

    public boolean isSQLSafetyFeature() {
        return security;
    }

}
