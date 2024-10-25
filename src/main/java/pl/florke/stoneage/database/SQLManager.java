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
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

@SuppressWarnings("CallToPrintStackTrace")
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
        final StringBuilder values = new StringBuilder();
        final StringBuilder keyDuplicate = new StringBuilder();

        // Init query
        query.append("INSERT INTO ")
                // "database_name.`table_name` ("
                .append(getDatabaseName()).append(".`").append(SQLManager.TABLE_PLAYER_DROP_CONFIG).append("` (");

        // Obligatory fields
        fields.append("`PlayerUUID`, "); // appends PlayerUUID field
        fields.append("`PlayerName`, "); // appends PlayerName field

        final Set<DropEntry> entries = config.getCustomDropEntries();

        // appends query details for each DropEntry
        for (Iterator<DropEntry> it = entries.iterator(); it.hasNext(); ) {
            DropEntry entry = it.next();
            if (entry == null) continue;

            // appends EntryName field to target columns list
            fields.append("`").append(entry.getEntryName()).append("`");

            // appends personal PlayerConfig values
            int dropSwitchStatus = config.isDropping(entry) ? 1 : 0;
            values.append("'").append(dropSwitchStatus).append("'"); // "'1'"

            // defaults
            keyDuplicate.append("`").append(entry.getEntryName()).append("`=VALUES")
                    .append("(`").append(entry.getEntryName()).append("`)");

            // appends "," if it's not the last target column
            if (it.hasNext()) {
                fields.append(", ");
                values.append(", ");
                keyDuplicate.append(", ");
            }
        }

        // append compiled target columns list to main query
        query.append(fields);

        // In next part, values are assigned for defined fields
        query.append(") VALUES (")
                .append("'").append(config.getUniqueId()).append("', ") // "'PLA73R-UU1D-ABC-XYZ', "
                .append("'").append(config.getPlayerName()).append("', ") // "'Florke64', "
                .append(values) // all compiled values put here at once
                .append(") ON DUPLICATE KEY UPDATE ")
                .append(keyDuplicate);

        return queryUpdate(query.toString());
    }

    public int runUpdateForPersonalStoneStats(@NotNull PlayerStats stats) {

        final StringBuilder query = new StringBuilder();
        final StringBuilder fields = new StringBuilder();
        final StringBuilder values = new StringBuilder();
        final StringBuilder keyDuplicate = new StringBuilder();

        // Init query
        query.append("INSERT INTO ")
                // "database_name.`table_name` ("
                .append(getDatabaseName()).append(".`").append(SQLManager.TABLE_PLAYER_STATS).append("` (");

        // Obligatory fields
        fields.append("`PlayerUUID`, ");
        fields.append("`PlayerName`, ");
        fields.append("`MinerExp`, ");
        fields.append("`MinerLvl`, ");

        for (final Iterator<String> it = stats.getStatisticKeys().iterator(); it.hasNext(); ) {
            final String entry = it.next();
            if (entry == null) continue;

            fields.append("`").append(entry).append("`"); // "`stone`"

            int dropStatistic = stats.getStatistic(entry);
            values.append("'").append(dropStatistic).append("'"); // "'1'"

            keyDuplicate.append("`").append(entry).append("`=VALUES(`").append(entry).append("`)");

            if (it.hasNext()) {
                fields.append(", ");
                query.append(", ");
                keyDuplicate.append(", ");
            }
        }

        query.append(fields);

        query.append(") VALUES (")
                .append("'").append(stats.getUniqueId()).append("', ") // "'PLA73R-UU1D-ABC-XYZ', "
                .append("'").append(stats.getPlayerName()).append("', ") // "'Florke64', "
                .append("'").append(stats.getMinerExp()).append("', ")  // "'1234', "
                .append("'").append(stats.getMinerLvl()).append("', ");  // "'4', "

        query.append(values).append(") ON DUPLICATE KEY UPDATE ");

        query.append("`MinerExp`=VALUES(`MinerExp`), ");
        query.append("`MinerLvl`=VALUES(`MinerLvl`), ");

        query.append(keyDuplicate);

        return queryUpdate(query.toString());
    }

    private int queryUpdate(@NotNull final String query) {
        try (final Connection conn = connectionPool.getConnection();
             final PreparedStatement ps = conn.prepareStatement(query)) {

            if (ps == null) return -1;

            final int response = ps.executeUpdate();

            if (!ps.isClosed()) {
                ps.close();
            }

            return response;
        } catch (SQLException | NullPointerException ex) {
            //Checks the error code and skipping exception's stack trace printing
            if (!(ex instanceof SQLException) || ((SQLException) ex).getErrorCode() != 1060) {
                ex.printStackTrace();
            }
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
        queryUpdate("CREATE DATABASE IF NOT EXISTS " + databaseName);
    }

    private void addTableColumnIfNotExist(final String tableName, final String columnName, final String columnType, final String defaultValue) {
        queryUpdate("ALTER TABLE " + getDatabaseName() + ".`" + tableName + "` " +
                "ADD COLUMN `" + columnName + "` " + columnType + " NOT NULL default " + defaultValue + ";");
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

        queryUpdate(query);
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

        queryUpdate(query);
    }

    private void makePlayerDropConfigTable() {
        final String databaseName = getDatabaseName();

        String query = "CREATE TABLE IF NOT EXISTS " + databaseName + "." + TABLE_PLAYER_DROP_CONFIG +
                " (" +
                " `PlayerUUID` VARCHAR(36)," +
                " `PlayerName` VARCHAR(16)," +
                " PRIMARY KEY (`PlayerUUID`)" +
                ") ";

        queryUpdate(query);
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

        queryUpdate(query);
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