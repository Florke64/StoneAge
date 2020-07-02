/*
 * Copyright Go2Play.pl (c) 2020.
 * Program made for Go2Play Skyblock server. It's not allowed to re-distribute the code.
 * Author: FlrQue
 */

package win.flrque.g2p.stoneage.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import win.flrque.g2p.stoneage.StoneAge;
import win.flrque.g2p.stoneage.util.ConfigSectionDatabase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ConnectionPoolManager {

    private final StoneAge plugin;
    private final ConfigSectionDatabase databaseConfig;

    private HikariDataSource dataSource;

    private String hostname;
    private int port;
    private String database;
    private String username;
    private String password;

    public ConnectionPoolManager(ConfigSectionDatabase databaseConfig) {
        this.plugin = StoneAge.getPlugin(StoneAge.class);
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
        HikariConfig config = new HikariConfig();
        config.setDataSourceClassName("org.mariadb.jdbc.MariaDbDataSource");

        config.setJdbcUrl("jdbc:mysql://" +hostname+ ":" +port+ "/" +database);
        config.setUsername(username);
        config.setPassword(password);

        try {
            dataSource = new HikariDataSource(config);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
    }

    public Connection getConnection() throws SQLException {
        return dataSource != null? dataSource.getConnection() : null;
    }

    public void close(Connection conn, PreparedStatement ps, ResultSet res) {
        if (conn != null) try { conn.close(); } catch (SQLException ignored) {}
        if (ps != null) try { ps.close(); } catch (SQLException ignored) {}
        if (res != null) try { res.close(); } catch (SQLException ignored) {}
    }

    public void closePool() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }

    public ConfigSectionDatabase getDatabaseConfig() {
        return databaseConfig;
    }
}
