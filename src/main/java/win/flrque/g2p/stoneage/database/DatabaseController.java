/*
 * Copyright Go2Play.pl (c) 2020.
 * Program made for Go2Play Skyblock server. It's not allowed to re-distribute the code.
 * Author: FlrQue
 */

package win.flrque.g2p.stoneage.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import win.flrque.g2p.stoneage.StoneAge;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;

public class DatabaseController {

    private final StoneAge plugin;

    private final HikariConfig hikariConfig;
    private final HikariDataSource hikari;

    public DatabaseController(HikariConfig config) {
        plugin = StoneAge.getPlugin(StoneAge.class);

        hikariConfig = config;
        plugin.getLogger().log(Level.INFO, "HikariConfig says: username=" + hikariConfig.getUsername());
        hikari = new HikariDataSource(hikariConfig);
    }

    public boolean createDatabase() {
        final String databaseName = hikari.getDataSourceProperties().getProperty("databaseName");

        final HikariQuery query = new HikariQuery("create database if not exists `"+databaseName+"` default charset utf8mb4", hikari);
//        try {
//            query.getStatement().setString(1, databaseName);
//        } catch (SQLException e) {
//            e.printStackTrace();
//            return false;
//        }

        return runQuery(query);
    }

    public boolean runQuery(HikariQuery query) {
        final Connection connection = query.getHikariConnection();
        if(connection == null) {
            plugin.getLogger().log(Level.SEVERE, "Couldn't get connection from pool!");
            return false;
        }

        final PreparedStatement statement = query.getStatement();

        plugin.getLogger().log(Level.INFO, "Running Query: " + statement.toString());
        plugin.getLogger().log(Level.INFO, "Running Query as user: " + hikari.getUsername());
        plugin.getLogger().log(Level.INFO, "Hikari DataSource Properties: ");
        for(Object prop : hikari.getDataSourceProperties().keySet()) {
            if(prop instanceof String) {
                final String val = hikari.getDataSourceProperties().getProperty((String) prop);

                plugin.getLogger().log(Level.INFO, "- " + prop + " = " + val);
            }
        }

        try {
            statement.execute();

        } catch (SQLException e) {
            e.printStackTrace();

        } finally {
            //Closing the connection
            if(connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

            //Closing the statement
            if(statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

            return true;
        }
    }

    public HikariDataSource getHikari() {
        return hikari;
    }
}
