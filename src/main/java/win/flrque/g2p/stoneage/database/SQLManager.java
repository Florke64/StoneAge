/*
 * Copyright Go2Play.pl (c) 2020.
 * Program made for Go2Play Skyblock server. It's not allowed to re-distribute the code.
 * Author: FlrQue
 */

package win.flrque.g2p.stoneage.database;

import win.flrque.g2p.stoneage.StoneAge;
import win.flrque.g2p.stoneage.drop.DropEntry;
import win.flrque.g2p.stoneage.util.ConfigSectionDatabase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;

public class SQLManager {

    private final StoneAge plugin;
    private final ConnectionPoolManager connectionPool;

    public static final String TABLE_PLAYER_STATS = "StoneAge_Stats";
    public static final String TABLE_PLAYER_DROP_CONFIG = "StoneAge_Config";

    public SQLManager(ConfigSectionDatabase databaseConfig) {
        this.plugin = StoneAge.getPlugin(StoneAge.class);
        connectionPool = new ConnectionPoolManager(databaseConfig);

        final String databaseName = connectionPool.getDatabaseConfig().getDatabaseName();

        try {
            makeDatabase(databaseName);

            makePlayerStatsTable();
            makePlayerDropConfigTable();

            for(DropEntry entry : plugin.getDropCalculator().getDropEntries()) {
                final String dropEntryName = entry.getEntryName();
                addTableColumnInNotExist(TABLE_PLAYER_STATS, dropEntryName, "INT", "0");
                addTableColumnInNotExist(TABLE_PLAYER_DROP_CONFIG, dropEntryName, "BOOLEAN", "true");
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to initialize db! Running without DB!");
            e.printStackTrace();
        }
    }

    private void makeDatabase(final String databaseName) {
        try (Connection conn = connectionPool.getConnection()) {
            final String query = "CREATE DATABASE IF NOT EXISTS " +databaseName;
            PreparedStatement ps = conn.prepareStatement(query);

            ps.executeUpdate();

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void addTableColumnInNotExist(final String tableName, final String columnName, final String columnType, final String defaultValue) {
        final String databaseName = connectionPool.getDatabaseConfig().getDatabaseName();

        try (Connection conn = connectionPool.getConnection()) {
            final StringBuilder query = new StringBuilder();

            query.append("ALTER TABLE " +databaseName+ ".`" +tableName+ "` ");
            query.append("ADD COLUMN IF NOT EXISTS `" +columnName+ "` " +columnType+ " NOT NULL default " +defaultValue+ ";");

            PreparedStatement ps = conn.prepareStatement(query.toString());

            ps.executeUpdate();

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void makePlayerDropConfigTable() {
        final String databaseName = connectionPool.getDatabaseConfig().getDatabaseName();

        try (Connection conn = connectionPool.getConnection()) {
            final StringBuilder query = new StringBuilder();

            query.append("CREATE TABLE IF NOT EXISTS " +databaseName+ "." +TABLE_PLAYER_DROP_CONFIG);
            query.append(" (");
            query.append(" PlayerUUID VARCHAR(36),");
            query.append(" PlayerName VARCHAR(16),");
            query.append(" PRIMARY KEY (`PlayerUUID`)");
            query.append(") ");

            PreparedStatement ps = conn.prepareStatement(query.toString());

            ps.executeUpdate();

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void makePlayerStatsTable() {
        final String databaseName = connectionPool.getDatabaseConfig().getDatabaseName();

        try (Connection conn = connectionPool.getConnection()) {
            final StringBuilder query = new StringBuilder();

            query.append("CREATE TABLE IF NOT EXISTS " +databaseName+ "." +TABLE_PLAYER_STATS);
            query.append(" (");
            query.append(" PlayerUUID VARCHAR(36),");
            query.append(" PlayerName VARCHAR(16),");
            query.append(" PRIMARY KEY (`PlayerUUID`)");
            query.append(") ");

            PreparedStatement ps = conn.prepareStatement(query.toString());

            ps.executeUpdate();

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public String getDataFromPlayerName(String playerName) throws SQLException{
        try (Connection conn = connectionPool.getConnection()){
            final String query = "SELECT data FROM Go2PlayLocalTesting WHERE `mcPlayerName` = ? LIMIT 1";
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setString(1, playerName);

            ResultSet rs = ps.executeQuery();

            while(rs.next()) {
                return rs.getString("data");
            }

            return "";

        }

    }

    public void onDisable() {
        connectionPool.closePool();
    }

}