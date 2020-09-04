/*
 * Copyright Go2Play.pl (c) 2020.
 * Program made for Go2Play Skyblock server. It's not allowed to re-distribute the code.
 * Author: FlrQue
 */

package win.flrque.g2p.stoneage.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import win.flrque.g2p.stoneage.StoneAge;
import win.flrque.g2p.stoneage.config.DatabaseConfigReader;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class ConnectionPoolManager {

    private final StoneAge plugin;
    private final DatabaseConfigReader databaseConfig;

    private HikariDataSource dataSource;

    private String hostname;
    private int port;
    private String database;
    private String username;
    private String password;

    public ConnectionPoolManager(DatabaseConfigReader databaseConfig) {
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
//        config.setDataSourceClassName("org.mariadb.jdbc.MariaDbDataSource");

//        config.setJdbcUrl("jdbc:mysql://" +hostname+ ":" +port+ "/" +database);
        config.setJdbcUrl("jdbc:mysql://" + hostname + "/" + database + "?user=" + username + "&password=" + password + "&useUnicode=true&characterEncoding=UTF-8&verifyServerCertificate=false&useSSL=false&requireSSL=false");
        config.setUsername(username);
        config.setPassword(password);
        config.setMaxLifetime(60000);
        config.setIdleTimeout(45000);
        config.setPoolName("StoneAgeDatabasePool");
        config.setConnectionTestQuery("SELECT 1;");
        config.addDataSourceProperty("autoReconnect", true);

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

    public void close(Connection connection, Statement statement, ResultSet resultSet) {
        if (connection != null) try { connection.close(); } catch (SQLException ignored) {}
        if (statement != null) try { statement.close(); } catch (SQLException ignored) {}
        if (resultSet != null) try { resultSet.close(); } catch (SQLException ignored) {}
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
