/*
 * Copyright Go2Play.pl (c) 2020.
 * Program made for Go2Play Skyblock server. It's not allowed to re-distribute the code.
 * Author: FlrQue
 */

package win.flrque.g2p.stoneage.database;

import win.flrque.g2p.stoneage.StoneAge;
import win.flrque.g2p.stoneage.database.playerdata.PersonalDropConfig;
import win.flrque.g2p.stoneage.database.playerdata.StoneMachinePlayerStats;
import win.flrque.g2p.stoneage.drop.DropEntry;
import win.flrque.g2p.stoneage.drop.DropMultiplier;
import win.flrque.g2p.stoneage.util.ConfigSectionDatabase;

import java.sql.*;
import java.util.Set;
import java.util.logging.Level;

public class SQLManager {

    private final StoneAge plugin;
    private final ConnectionPoolManager connectionPool;

    public static final String TABLE_PLAYER_STATS = "StoneAge_Stats";
    public static final String TABLE_PLAYER_DROP_CONFIG = "StoneAge_Config";
    public static final String TABLE_DROP_MULTIPLIER = "StoneAge_DropMultiplier";

    public SQLManager(ConfigSectionDatabase databaseConfig) {
        this.plugin = StoneAge.getPlugin(StoneAge.class);
        connectionPool = new ConnectionPoolManager(databaseConfig);

        try {
            init();
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to initialize db! Running without DB!");
            e.printStackTrace();
        }
    }

    public int runUpdateForPersonalDropConfig(PersonalDropConfig config) throws SQLException {
        try (Connection conn = connectionPool.getConnection()){
            if(conn == null) return -1;

            final StringBuilder query = new StringBuilder();
            final StringBuilder fields = new StringBuilder();
            query.append("INSERT INTO ").append(getDatabaseName() + ".`" +SQLManager.TABLE_PLAYER_DROP_CONFIG+ "` (");

            fields.append("`PlayerUUID`, ");
            fields.append("`PlayerName`, ");

            final Set<DropEntry> entries = config.getCustomDropEntries();

            int i = 0;
            for(DropEntry entry : entries) {
                i++;
                if(entry == null) continue;

                fields.append("`" +entry.getEntryName()+ "`");
                if(i < entries.size()) {
                    fields.append(", ");
                }
            }

            query.append(fields);

            i = 0;
            query.append(") VALUES (");
            query.append("'" +config.getUniqueId()+ "', ");
            query.append("'" +config.getPlayerName()+ "', ");
            for(DropEntry entry : entries) {
                i++;
                if(entry == null) continue;

                int dropSwitchStatus = config.isDropping(entry)? 1 : 0;
                query.append("'" +dropSwitchStatus+ "'");
                if(i < entries.size()) {
                    query.append(", ");
                }
            }
            query.append(") ON DUPLICATE KEY UPDATE ");

            i = 0;
            for(DropEntry entry : entries) {
                i++;
                if(entry == null) continue;

                query.append("`" +entry.getEntryName()+ "`=VALUES(`" +entry.getEntryName()+ "`)");
                if(i < entries.size()) {
                    query.append(", ");
                }
            }

            PreparedStatement ps = conn.prepareStatement(query.toString());

            final int response = ps.executeUpdate();

            return response;
        }
    }

    public int runUpdateForPersonalStoneStats(StoneMachinePlayerStats stats) throws SQLException {
        try (Connection conn = connectionPool.getConnection()){
            if(conn == null) return -1;

            final StringBuilder query = new StringBuilder();
            final StringBuilder fields = new StringBuilder();
            query.append("INSERT INTO ").append(getDatabaseName() + ".`" +SQLManager.TABLE_PLAYER_STATS+ "` (");

            fields.append("`PlayerUUID`, ");
            fields.append("`PlayerName`, ");

            final Set<String> entries = stats.getStatisticKeys();

            int i = 0;
            for(String key : entries) {
                i++;
                if(key == null) continue;

                fields.append("`" +key+ "`");
                if(i < entries.size()) {
                    fields.append(", ");
                }
            }

            query.append(fields);

            i = 0;
            query.append(") VALUES (");
            query.append("'" +stats.getUniqueId()+ "', ");
            query.append("'" +stats.getPlayerName()+ "', ");
            for(String key : entries) {
                i++;
                if(key == null) continue;

                int dropStatistic = stats.getStatistic(key);
                query.append("'" +dropStatistic+ "'");
                if(i < entries.size()) {
                    query.append(", ");
                }
            }
            query.append(") ON DUPLICATE KEY UPDATE ");

            i = 0;
            for(String key : entries) {
                i++;
                if(key == null) continue;

                query.append("`" +key+ "`=VALUES(`" +key+ "`)");
                if(i < entries.size()) {
                    query.append(", ");
                }
            }

            PreparedStatement ps = conn.prepareStatement(query.toString());

            final int response = ps.executeUpdate();

            return response;
        }
    }

    public ResultSet runQuery(final String query) throws SQLException {
        try (Connection conn = connectionPool.getConnection()){
            if(conn == null) return null;

            PreparedStatement ps = conn.prepareStatement(query);

            final ResultSet rs = ps.executeQuery();

            return rs;
        }
    }

    private void init() {
        final String databaseName = getDatabaseName();

        makeDatabase(databaseName);

        makePlayerStatsTable();
        makePlayerDropConfigTable();
        makeDropMultiplierTable();

        for(DropEntry entry : plugin.getDropCalculator().getDropEntries()) {
            final String dropEntryName = entry.getEntryName();
            addTableColumnInNotExist(TABLE_PLAYER_STATS, dropEntryName, "INT", "0");
            addTableColumnInNotExist(TABLE_PLAYER_DROP_CONFIG, dropEntryName, "BOOLEAN", "true");
        }

        addTableColumnInNotExist(TABLE_PLAYER_STATS, "primitive_drop", "INT", "0");
        addTableColumnInNotExist(TABLE_PLAYER_DROP_CONFIG, "primitive_drop", "BOOLEAN", "true");
    }

    private void makeDatabase(final String databaseName) {
        try (Connection conn = connectionPool.getConnection()) {
            if(conn == null) return;

            final String query = "CREATE DATABASE IF NOT EXISTS " +databaseName;
            PreparedStatement ps = conn.prepareStatement(query);

            ps.executeUpdate();

        } catch (SQLException | NullPointerException ex) {
            ex.printStackTrace();
        }
    }

    private void addTableColumnInNotExist(final String tableName, final String columnName, final String columnType, final String defaultValue) {
        final String databaseName = getDatabaseName();

        try (Connection conn = connectionPool.getConnection()) {
            if(conn == null) return;

            final StringBuilder query = new StringBuilder();

            query.append("ALTER TABLE " +databaseName+ ".`" +tableName+ "` ");
            query.append("ADD COLUMN IF NOT EXISTS `" +columnName+ "` " +columnType+ " NOT NULL default " +defaultValue+ ";");

            PreparedStatement ps = conn.prepareStatement(query.toString());

            ps.executeUpdate();

        } catch (SQLException | NullPointerException ex) {
            ex.printStackTrace();
        }
    }

    private void makeDropMultiplierTable() {
        final String databaseName = getDatabaseName();

        try (Connection conn = connectionPool.getConnection()) {
            if(conn == null) return;

            final StringBuilder query = new StringBuilder();

            query.append("CREATE TABLE IF NOT EXISTS `" +databaseName+ "`.`" + TABLE_DROP_MULTIPLIER + "`");
            query.append(" (");
            query.append(" `MultiplierId` INT NOT NULL AUTO_INCREMENT,");
            query.append(" `SetOn` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,");
            query.append(" `Timeout` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,");
            query.append(" `MultiplierValue` FLOAT NOT NULL DEFAULT '1.0',");
            query.append(" PRIMARY KEY (`MultiplierId`)");
            query.append(") ");

            PreparedStatement ps = conn.prepareStatement(query.toString());

            ps.executeUpdate();

        } catch (SQLException | NullPointerException ex) {
            ex.printStackTrace();
        }
    }

    public void insertDropMultiplierRecord(DropMultiplier dropMultiplier) throws SQLException {

        try (Connection conn = connectionPool.getConnection()){
            if(conn == null) return;

            final Timestamp startTime = new Timestamp(dropMultiplier.getMultiplierStartTime());
            final Timestamp timeoutTime = new Timestamp(dropMultiplier.getMultiplierTimeout());

            final StringBuilder query = new StringBuilder();
            query.append("INSERT INTO ").append(getDatabaseName() + ".`" +SQLManager.TABLE_PLAYER_STATS+ "` ");

            query.append(" (`MultiplierId`, `SetOn`, `Timeout`, `MultiplierValue`) VALUES (NULL,");
            query.append(" '" +startTime+ "',");
            query.append(" '" +timeoutTime+ "',");
            query.append(" '" +dropMultiplier.getCurrentDropMultiplier()+ "');");

            PreparedStatement ps = conn.prepareStatement(query.toString());

            ps.execute();

        }
    }

    private void makePlayerDropConfigTable() {
        final String databaseName = getDatabaseName();

        try (Connection conn = connectionPool.getConnection()) {
            if(conn == null) return;

            final StringBuilder query = new StringBuilder();

            query.append("CREATE TABLE IF NOT EXISTS " +databaseName+ "." +TABLE_PLAYER_DROP_CONFIG);
            query.append(" (");
            query.append(" PlayerUUID VARCHAR(36),");
            query.append(" PlayerName VARCHAR(16),");
            query.append(" PRIMARY KEY (`PlayerUUID`)");
            query.append(") ");

            PreparedStatement ps = conn.prepareStatement(query.toString());

            ps.executeUpdate();

        } catch (SQLException | NullPointerException ex) {
            ex.printStackTrace();
        }
    }

    private void makePlayerStatsTable() {
        final String databaseName = connectionPool.getDatabaseConfig().getDatabaseName();

        try (Connection conn = connectionPool.getConnection()) {
            if(conn == null) return;

            final StringBuilder query = new StringBuilder();

            query.append("CREATE TABLE IF NOT EXISTS " +databaseName+ "." +TABLE_PLAYER_STATS);
            query.append(" (");
            query.append(" PlayerUUID VARCHAR(36),");
            query.append(" PlayerName VARCHAR(16),");
            query.append(" PRIMARY KEY (`PlayerUUID`)");
            query.append(") ");

            PreparedStatement ps = conn.prepareStatement(query.toString());

            ps.executeUpdate();

        } catch (SQLException | NullPointerException ex) {
            ex.printStackTrace();
        }
    }

    public void onDisable() {
        if(connectionPool != null) {
            connectionPool.closePool();
        }
    }

    public String getDatabaseName() {
        return connectionPool.getDatabaseConfig().getDatabaseName();
    }

}