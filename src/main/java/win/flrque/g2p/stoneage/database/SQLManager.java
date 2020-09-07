/*
 * Copyright Go2Play.pl (c) 2020.
 * Program made for Go2Play Skyblock server. It's not allowed to re-distribute the code.
 * Author: FlrQue
 */

package win.flrque.g2p.stoneage.database;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import win.flrque.g2p.stoneage.StoneAge;
import win.flrque.g2p.stoneage.config.DatabaseConfigReader;
import win.flrque.g2p.stoneage.database.playerdata.PlayerConfig;
import win.flrque.g2p.stoneage.database.playerdata.PlayerStats;
import win.flrque.g2p.stoneage.drop.DropEntry;
import win.flrque.g2p.stoneage.util.Message;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

public class SQLManager {

    private final StoneAge plugin;
    private final ConnectionPoolManager connectionPool;

    private BukkitRunnable autosaveRunnable;

    public static final String TABLE_PLAYER_STATS = "stoneage_stats";
    public static final String TABLE_PLAYER_DROP_CONFIG = "stoneage_config";
    public static final String TABLE_DROP_MULTIPLIER = "stoneage_multiplier";

    public SQLManager(DatabaseConfigReader databaseConfig) {
        this.plugin = StoneAge.getPlugin(StoneAge.class);
        connectionPool = new ConnectionPoolManager(databaseConfig);

        try {
            init();
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to initialize db! Running without DB!");
            e.printStackTrace();
        }
    }

    public int runUpdateForPersonalDropConfig(PlayerConfig config) {

        final StringBuilder query = new StringBuilder();
        final StringBuilder fields = new StringBuilder();
        query.append("INSERT INTO ").append(getDatabaseName() + ".`" + SQLManager.TABLE_PLAYER_DROP_CONFIG + "` (");

        fields.append("`PlayerUUID`, ");
        fields.append("`PlayerName`, ");

        final Set<DropEntry> entries = config.getCustomDropEntries();

        int i = 0;
        for (DropEntry entry : entries) {
            i++;
            if (entry == null) continue;

            fields.append("`" + entry.getEntryName() + "`");
            if (i < entries.size()) {
                fields.append(", ");
            }
        }

        query.append(fields);

        i = 0;
        query.append(") VALUES (");
        query.append("'" + config.getUniqueId() + "', ");
        query.append("'" + config.getPlayerName() + "', ");
        for (DropEntry entry : entries) {
            i++;
            if (entry == null) continue;

            int dropSwitchStatus = config.isDropping(entry) ? 1 : 0;
            query.append("'" + dropSwitchStatus + "'");
            if (i < entries.size()) {
                query.append(", ");
            }
        }
        query.append(") ON DUPLICATE KEY UPDATE ");

        i = 0;
        for (DropEntry entry : entries) {
            i++;
            if (entry == null) continue;

            query.append("`" + entry.getEntryName() + "`=VALUES(`" + entry.getEntryName() + "`)");
            if (i < entries.size()) {
                query.append(", ");
            }
        }

        try (final Connection conn = connectionPool.getConnection();
             final PreparedStatement ps = conn.prepareStatement(query.toString())) {

            if (conn == null || ps == null) return -1;

            final int response = ps.executeUpdate();

            if (ps != null && !ps.isClosed()) {
                ps.close();
            }

            return response;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return -1;
    }

    public int runUpdateForPersonalStoneStats(PlayerStats stats) {

        final StringBuilder query = new StringBuilder();
        final StringBuilder fields = new StringBuilder();
        query.append("INSERT INTO ").append(getDatabaseName() + ".`" + SQLManager.TABLE_PLAYER_STATS + "` (");

        fields.append("`PlayerUUID`, ");
        fields.append("`PlayerName`, ");
        fields.append("`MinerExp`, ");
        fields.append("`MinerLvl`, ");

        final Set<String> entries = stats.getStatisticKeys();

        int i = 0;
        for (String key : entries) {
            i++;
            if (key == null) continue;

            fields.append("`" + key + "`");
            if (i < entries.size()) {
                fields.append(", ");
            }
        }

        query.append(fields);

        i = 0;
        query.append(") VALUES (");
        query.append("'" + stats.getUniqueId() + "', ");
        query.append("'" + stats.getPlayerName() + "', ");
        query.append("'" + stats.getMinerExp() + "', ");
        query.append("'" + stats.getMinerLvl() + "', ");
        for (String key : entries) {
            i++;
            if (key == null) continue;

            int dropStatistic = stats.getStatistic(key);
            query.append("'" + dropStatistic + "'");
            if (i < entries.size()) {
                query.append(", ");
            }
        }
        query.append(") ON DUPLICATE KEY UPDATE ");

        query.append("`MinerExp`=VALUES(`MinerExp`), ");
        query.append("`MinerLvl`=VALUES(`MinerLvl`), ");

        i = 0;
        for (String key : entries) {
            i++;
            if (key == null) continue;

            query.append("`" + key + "`=VALUES(`" + key + "`)");
            if (i < entries.size()) {
                query.append(", ");
            }
        }

        try (final Connection conn = connectionPool.getConnection();
             final PreparedStatement ps = conn.prepareStatement(query.toString())) {
            if (conn == null || ps == null) return -1;

            final int response = ps.executeUpdate();

            if (ps != null && !ps.isClosed()) {
                ps.close();
            }

            return response;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return -1;
    }

    private void init() {
        final String databaseName = getDatabaseName();

        makeDatabase(databaseName);

        makePlayerStatsTable();
        makePlayerDropConfigTable();
        makeDropMultiplierTable();

        for (DropEntry entry : plugin.getDropCalculator().getDropEntries()) {
            final String dropEntryName = entry.getEntryName();
            addTableColumnIfNotExist(TABLE_PLAYER_STATS, dropEntryName, "INT", "0");
            addTableColumnIfNotExist(TABLE_PLAYER_DROP_CONFIG, dropEntryName, "BOOLEAN", "true");
        }

        addTableColumnIfNotExist(TABLE_PLAYER_STATS, "primitive_drop", "INT", "0");
        addTableColumnIfNotExist(TABLE_PLAYER_DROP_CONFIG, "primitive_drop", "BOOLEAN", "true");
    }

    public Connection getConnection() throws SQLException {
        return connectionPool.getConnection();
    }

    private void makeDatabase(final String databaseName) {
        final String query = "CREATE DATABASE IF NOT EXISTS " + databaseName;

        try (final Connection conn = connectionPool.getConnection();
             final PreparedStatement ps = conn.prepareStatement(query)) {
            if (conn == null) return;

            ps.executeUpdate();

        } catch (SQLException | NullPointerException ex) {
            ex.printStackTrace();
        }
    }

    private void addTableColumnIfNotExist(final String tableName, final String columnName, final String columnType, final String defaultValue) {
        final String databaseName = getDatabaseName();

        final StringBuilder query = new StringBuilder();

        query.append("ALTER TABLE " + databaseName + ".`" + tableName + "` ");
        query.append("ADD COLUMN `" + columnName + "` " + columnType + " NOT NULL default " + defaultValue + ";");

        try (final Connection conn = connectionPool.getConnection();
             final PreparedStatement ps = conn.prepareStatement(query.toString())) {
            if (conn == null || ps == null) return;

            ps.executeUpdate();

        } catch (SQLException | NullPointerException ex) {
            //Checks the error code and skipping exception's stack trace printing
            if (!(ex instanceof SQLException) || ((SQLException) ex).getErrorCode() != 1060) {
                ex.printStackTrace();
            }
        }
    }

    private void makeDropMultiplierTable() {
        final String databaseName = getDatabaseName();

        final StringBuilder query = new StringBuilder();

        query.append("CREATE TABLE IF NOT EXISTS `" + databaseName + "`.`" + TABLE_DROP_MULTIPLIER + "`");
        query.append(" (");
        query.append(" `MultiplierId` INT NOT NULL AUTO_INCREMENT,");
        query.append(" `SetOn` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,");
        query.append(" `Timeout` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,");
        query.append(" `MultiplierValue` FLOAT NOT NULL DEFAULT '1.0',");
        query.append(" `CallerName` VARCHAR(16),");
        query.append(" `CallerUUID` VARCHAR(36),");
        query.append(" PRIMARY KEY (`MultiplierId`)");
        query.append(") ");

        try (final Connection conn = connectionPool.getConnection();
             final PreparedStatement ps = conn.prepareStatement(query.toString())) {
            if (conn == null || ps == null) return;
            ps.executeUpdate();

        } catch (SQLException | NullPointerException ex) {
            ex.printStackTrace();
        }
    }

    public void insertDropMultiplierRecord(String callerName, @NotNull UUID callerUniqueId, float value, long start, long end) throws SQLException {
        final Timestamp startTime = new Timestamp(start);
        final Timestamp timeoutTime = new Timestamp(end);

        final StringBuilder query = new StringBuilder();
        query.append("INSERT INTO ").append(getDatabaseName() + ".`" + SQLManager.TABLE_DROP_MULTIPLIER + "` ");

        query.append(" (`MultiplierId`, `SetOn`, `Timeout`, `MultiplierValue`, `CallerName`, `CallerUUID`) VALUES (NULL,");
        query.append(" '" + startTime + "',");
        query.append(" '" + timeoutTime + "',");
        query.append(" '" + value + "', ");
        query.append(" '" + callerName + "', ");
        query.append(" '" + callerUniqueId.toString() + "');");

        try (final Connection conn = connectionPool.getConnection();
             final PreparedStatement ps = conn.prepareStatement(query.toString())) {
            if (conn == null || ps == null) return;

            ps.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void makePlayerDropConfigTable() {
        final String databaseName = getDatabaseName();

        final StringBuilder query = new StringBuilder();

        query.append("CREATE TABLE IF NOT EXISTS " + databaseName + "." + TABLE_PLAYER_DROP_CONFIG);
        query.append(" (");
        query.append(" `PlayerUUID` VARCHAR(36),");
        query.append(" `PlayerName` VARCHAR(16),");
        query.append(" PRIMARY KEY (`PlayerUUID`)");
        query.append(") ");

        try (final Connection conn = connectionPool.getConnection();
             final PreparedStatement ps = conn.prepareStatement(query.toString())) {
            if (conn == null || ps == null) return;

            ps.executeUpdate();

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void makePlayerStatsTable() {
        final String databaseName = connectionPool.getDatabaseConfig().getDatabaseName();

        final StringBuilder query = new StringBuilder();

        query.append("CREATE TABLE IF NOT EXISTS " + databaseName + "." + TABLE_PLAYER_STATS);
        query.append(" (");
        query.append(" `PlayerUUID` VARCHAR(36),");
        query.append(" `PlayerName` VARCHAR(16),");
        query.append(" `MinerExp` BIGINT UNSIGNED NOT NULL DEFAULT '0',");
        query.append(" `MinerLvl` INT UNSIGNED NOT NULL DEFAULT '1',");
        query.append(" PRIMARY KEY (`PlayerUUID`)");
        query.append(") ");

        try (final Connection conn = connectionPool.getConnection();
             final PreparedStatement ps = conn.prepareStatement(query.toString())) {
            if (conn == null || ps == null) return;

            ps.executeUpdate();

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public void initAsyncAutosave(final long period) {

        plugin.getLogger().log(Level.INFO, "Initialized Async Autosave.");

        autosaveRunnable = new BukkitRunnable() {

            @Override
            public void run() {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        final int savedCount = saveAllOnlinePlayersData();

                        final Message success = new Message("Auto-save: Saved $_1 players data into the database | Next Auto-Save in $_2 minutes");
                        success.setVariable(1, Integer.toString(savedCount));
                        success.setVariable(2, Long.toString(period));

                        plugin.getLogger().log(Level.INFO, success.getPreparedMessage().get(0));
                    }
                }.runTaskAsynchronously(plugin);
            }
        };

        autosaveRunnable.runTaskTimer(plugin, period * 60 * 20, period * 60 * 20);
    }

    public int saveAllOnlinePlayersData() {
        int savedCount = 0;

        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            final UUID playerUUID = player.getUniqueId();
            final PlayerConfig dropConfig = plugin.getPlayersData().getPersonalDropConfig(playerUUID);
            final PlayerStats dropStats = plugin.getPlayersData().getPlayerStoneMachineStats(playerUUID);

            plugin.getPlayersData().savePersonalDropConfigInDatabase(dropConfig);
            plugin.getPlayersData().savePersonalStoneStatsInDatabase(dropStats);

            savedCount++;
        }

        return savedCount;
    }

    public BukkitRunnable getAutosaveRunnable() {
        return autosaveRunnable;
    }

    public void onDisable() {
        if (connectionPool != null) {
            connectionPool.closePool();
        }
    }

    public String getDatabaseName() {
        return connectionPool.getDatabaseConfig().getDatabaseName();
    }

}