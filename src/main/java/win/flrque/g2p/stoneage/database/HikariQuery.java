/*
 * Copyright Go2Play.pl (c) 2020.
 * Program made for Go2Play Skyblock server. It's not allowed to re-distribute the code.
 * Author: FlrQue
 */

package win.flrque.g2p.stoneage.database;

import com.zaxxer.hikari.HikariDataSource;
import win.flrque.g2p.stoneage.StoneAge;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;

public class HikariQuery {

    private final StoneAge plugin;

    private String query;
    private PreparedStatement statement;
    private HikariDataSource hikariDataSource;

    public HikariQuery(String query, HikariDataSource hikariDataSource) {
        plugin = StoneAge.getPlugin(StoneAge.class);

        this.hikariDataSource = hikariDataSource;
        Connection hikariConnection = null;
        statement = null;

        try {
            hikariConnection = this.hikariDataSource.getConnection();
            statement = hikariConnection.prepareStatement(query);

            if(statement == null) {
                plugin.getLogger().log(Level.WARNING, "Query Statement is null!");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Connection getHikariConnection() {
        try {
            return hikariDataSource.getConnection(hikariDataSource.getUsername(), hikariDataSource.getPassword());
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public PreparedStatement getStatement() {
        return statement;
    }
}
