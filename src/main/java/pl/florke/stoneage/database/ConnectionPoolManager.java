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

package pl.florke.stoneage.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import pl.florke.stoneage.config.DatabaseConfigReader;

import java.sql.Connection;
import java.sql.SQLException;

public class ConnectionPoolManager {

    private final DatabaseConfigReader databaseConfig;

    private HikariDataSource dataSource;

    private String hostname;
    private int port;
    private String database;
    private String username;
    private String password;

    public ConnectionPoolManager(DatabaseConfigReader databaseConfig) {
        this.databaseConfig = databaseConfig;

        init();
        setupPool();
    }

    private void init() {
        hostname = databaseConfig.getServerAddress();
        port = databaseConfig.getServerPort();
        database = databaseConfig.getDatabaseName();
        username = databaseConfig.getUsername();
        password = databaseConfig.getPassword();
    }

    private void setupPool() {
        final HikariConfig config = new HikariConfig();
        config.setPoolName("StoneAgeDatabasePool");
//        config.setDataSourceClassName("org.mariadb.jdbc.MariaDbDataSource");

        config.setJdbcUrl("jdbc:mysql://" + hostname + ":" + port + "/" + database);

        config.setUsername(username);
        config.setPassword(password);

        config.addDataSourceProperty("useUnicode", true);
        config.addDataSourceProperty("characterEncoding", "UTF-8");
        config.addDataSourceProperty("autoReconnect", true);

        if (!databaseConfig.isSQLSafetyFeature()) {
            config.addDataSourceProperty("verifyServerCertificate", "false");
            config.addDataSourceProperty("useSSL", "false");
            config.addDataSourceProperty("requireSSL", "false");
        }

        config.setConnectionTestQuery("SELECT 1;");

        try {
            dataSource = new HikariDataSource(config);
        } catch (Exception e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
    }

    public Connection getConnection() throws SQLException {
        return (dataSource != null) ? dataSource.getConnection() : null;
    }

    public void closePool() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }

    public DatabaseConfigReader getDatabaseConfig() {
        return databaseConfig;
    }
}