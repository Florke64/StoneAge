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

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import pl.florke.stoneage.StoneAge;
import pl.florke.stoneage.config.DatabaseConfigReader;
import pl.florke.stoneage.database.playerdata.PlayerConfig;
import pl.florke.stoneage.database.playerdata.PlayerStats;
import pl.florke.stoneage.drop.DropEntry;
import pl.florke.stoneage.util.Message;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

public class SQLManager {

    public static final String TABLE_PLAYER_STATS = "stoneage_stats";
    public static final String TABLE_PLAYER_DROP_CONFIG = "stoneage_config";
    public static final String TABLE_DROP_MULTIPLIER = "stoneage_multiplier";
    private final StoneAge plugin;
    private final ConnectionPoolManager connectionPool;
    private BukkitRunnable autosaveRunnable;

    public SQLManager(DatabaseConfigReader databaseConfig) {
        this.plugin = StoneAge.getPlugin(StoneAge.class);
        connectionPool = new ConnectionPoolManager(databaseConfig);

        try {
            init();
        } catch (Exception e) {
            new Message("Failed to initialize db! Running without DB!").log(Level.SEVERE);
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
    }

    public int runUpdateForPersonalDropConfig(@NotNull PlayerConfig config) {

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

    public int runUpdateForPersonalStoneStats(@NotNull PlayerStats stats) {

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

        String query = "ALTER TABLE " + databaseName + ".`" + tableName + "` " +
                "ADD COLUMN `" + columnName + "` " + columnType + " NOT NULL default " + defaultValue + ";";

        try (final Connection conn = connectionPool.getConnection();
             final PreparedStatement ps = conn.prepareStatement(query)) {
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

        String query = "CREATE TABLE IF NOT EXISTS `" + databaseName + "`.`" + TABLE_DROP_MULTIPLIER + "`" +
                " (" +
                " `MultiplierId` INT NOT NULL AUTO_INCREMENT," +
                " `SetOn` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                " `Timeout` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                " `MultiplierValue` FLOAT NOT NULL DEFAULT '1.0'," +
                " `CallerName` VARCHAR(16)," +
                " `CallerUUID` VARCHAR(36)," +
                " PRIMARY KEY (`MultiplierId`)" +
                ") ";

        try (final Connection conn = connectionPool.getConnection();
             final PreparedStatement ps = conn.prepareStatement(query)) {
            if (conn == null || ps == null) return;
            ps.executeUpdate();

        } catch (SQLException | NullPointerException ex) {
            ex.printStackTrace();
        }
    }

    public void insertDropMultiplierRecord(String callerName, @NotNull UUID callerUniqueId, float value, long start, long end) throws SQLException {
        final Timestamp startTime = new Timestamp(start);
        final Timestamp timeoutTime = new Timestamp(end);

        String query = "INSERT INTO " + getDatabaseName() + ".`" + SQLManager.TABLE_DROP_MULTIPLIER + "` " +
                " (`MultiplierId`, `SetOn`, `Timeout`, `MultiplierValue`, `CallerName`, `CallerUUID`) VALUES (NULL," +
                " '" + startTime + "'," +
                " '" + timeoutTime + "'," +
                " '" + value + "', " +
                " '" + callerName + "', " +
                " '" + callerUniqueId + "');";

        try (final Connection conn = connectionPool.getConnection();
             final PreparedStatement ps = conn.prepareStatement(query)) {
            if (conn == null || ps == null) return;

            ps.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void makePlayerDropConfigTable() {
        final String databaseName = getDatabaseName();

        String query = "CREATE TABLE IF NOT EXISTS " + databaseName + "." + TABLE_PLAYER_DROP_CONFIG +
                " (" +
                " `PlayerUUID` VARCHAR(36)," +
                " `PlayerName` VARCHAR(16)," +
                " PRIMARY KEY (`PlayerUUID`)" +
                ") ";

        try (final Connection conn = connectionPool.getConnection();
             final PreparedStatement ps = conn.prepareStatement(query)) {
            if (conn == null || ps == null) return;

            ps.executeUpdate();

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void makePlayerStatsTable() {
        final String databaseName = connectionPool.getDatabaseConfig().getDatabaseName();

        String query = "CREATE TABLE IF NOT EXISTS " + databaseName + "." + TABLE_PLAYER_STATS +
                " (" +
                " `PlayerUUID` VARCHAR(36)," +
                " `PlayerName` VARCHAR(16)," +
                " `MinerExp` BIGINT UNSIGNED NOT NULL DEFAULT '0'," +
                " `MinerLvl` INT UNSIGNED NOT NULL DEFAULT '1'," +
                " PRIMARY KEY (`PlayerUUID`)" +
                ") ";

        try (final Connection conn = connectionPool.getConnection();
             final PreparedStatement ps = conn.prepareStatement(query)) {
            if (conn == null || ps == null) return;

            ps.executeUpdate();

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public void initAsyncAutosave(final long period) {

        new Message("Initialized Async Autosave.").log(Level.INFO);

        autosaveRunnable = new BukkitRunnable() {

            @Override
            public void run() {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        final int savedCount = saveAllOnlinePlayersData();

                        new Message("Saved $_1 players in database. Next Auto-Save: $_2m")
                            .placeholder(1, Integer.toString(savedCount))
                            .placeholder(2, Long.toString(period))
                                .log(Level.INFO);

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