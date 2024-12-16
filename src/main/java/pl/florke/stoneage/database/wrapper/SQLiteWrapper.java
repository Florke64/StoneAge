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

package pl.florke.stoneage.database.wrapper;

import com.zaxxer.hikari.HikariConfig;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import pl.florke.stoneage.StoneAge;
import pl.florke.stoneage.config.DatabaseConfigReader;
import pl.florke.stoneage.database.DatabaseManager;
import pl.florke.stoneage.database.playerdata.PlayerConfig;
import pl.florke.stoneage.database.playerdata.PlayerStats;
import pl.florke.stoneage.database.playerdata.PlayersData;
import pl.florke.stoneage.drop.DropCalculator;
import pl.florke.stoneage.drop.DropEntry;
import pl.florke.stoneage.drop.DropEntryManager;
import pl.florke.stoneage.drop.DropMultiplier;
import pl.florke.stoneage.util.Message;

import java.sql.*;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

public class SQLiteWrapper extends DatabaseWrapper {

    private final StoneAge plugin = StoneAge.getPlugin(StoneAge.class);

    public SQLiteWrapper(@NotNull DatabaseConfigReader databaseConfig) {
        super(databaseConfig);
        initDatabase();
    }

    private void initDatabase() {
        makePlayerStatsTable();
        makePlayerConfigTable();
        makeDropMultiplierTable();

        final DropEntryManager dropEntryManager = plugin.getDropCalculator().getDropEntryManager();

        for (DropEntry entry : dropEntryManager.getCustomDropEntries()) {
            final String dropEntryName = entry.getKey().getKey();
            addTableColumnIfNotExist(DatabaseManager.TABLE_PLAYER_STATS, dropEntryName, "INT", "0");
            addTableColumnIfNotExist(DatabaseManager.TABLE_PLAYER_CONFIG, dropEntryName, "BOOLEAN", "true");
        }

        for (DropEntry entry : dropEntryManager.getDropResourcesEntries().values()) {
            final String dropEntryName = entry.getKey().getKey();
            addTableColumnIfNotExist(DatabaseManager.TABLE_PLAYER_STATS, dropEntryName, "INT", "0");
            addTableColumnIfNotExist(DatabaseManager.TABLE_PLAYER_CONFIG, dropEntryName, "BOOLEAN", "true");
        }
    }

    protected HikariConfig getHikariConfig(@NotNull DatabaseConfigReader databaseConfig) {
        final HikariConfig config = new HikariConfig();
        config.setPoolName("StoneAgeDatabasePool");

        // Saving to plugins/StoneAge/database.sqlite
        config.setJdbcUrl("jdbc:sqlite:"
//                FIXME: plugin isn't initialized?
//                + plugin.getDataPath() + "/"
                + StoneAge.getPlugin(StoneAge.class).getDataPath() + "/"
                + databaseConfig.getDatabaseName()
                + ".sqlite");

        config.addDataSourceProperty("useUnicode", true);
        config.addDataSourceProperty("characterEncoding", "UTF-8");

        config.setConnectionTestQuery("SELECT 1;");

        return config;
    }

    public int updatePlayerConfig(@NotNull PlayerConfig config) {
        final StringBuilder query = new StringBuilder();
        final StringBuilder fields = new StringBuilder();
        final StringBuilder values = new StringBuilder();

        // Initialize query
        query.append("INSERT OR REPLACE INTO ")
                .append("`").append(DatabaseManager.TABLE_PLAYER_CONFIG).append("` (");

        // Obligatory fields
        fields.append("`PlayerUUID`, `PlayerName`"); // adds PlayerUUID and PlayerName fields

        values.append("'").append(config.getUniqueId()).append("', "); // UUID value
        values.append("'").append(config.getPlayerName()).append("'"); // PlayerName value

        final Set<DropEntry> entries = config.getCustomDropEntries();

        // Append each DropEntry to query
        for (DropEntry entry : entries) {
            if (entry == null) continue;

            // Append entry name to target columns
            fields.append(", `").append(entry.getKey().getKey()).append("`");

            // Append PlayerConfig value for each entry
            int dropSwitchStatus = config.isDropping(entry) ? 1 : 0;
            values.append(", '").append(dropSwitchStatus).append("'"); // '1' or '0'
        }

        // Complete main query
        query.append(fields)
                .append(") VALUES (")
                .append(values).append(")");

        // Execute query
        return DatabaseManager.queryUpdate(getHikariDataSource(), query.toString());
    }

    public int updatePlayerStats(@NotNull PlayerStats stats) {
        final StringBuilder query = new StringBuilder();
        final StringBuilder fields = new StringBuilder();
        final StringBuilder values = new StringBuilder();

        // Init query
        query.append("INSERT OR REPLACE INTO `")
                .append(DatabaseManager.TABLE_PLAYER_STATS)
                .append("` (");

        // Obligatory fields
        fields.append("`PlayerUUID`, `PlayerName`, `MinerExp`, `MinerLvl`");
        values.append("'").append(stats.getUniqueId()).append("', ")
                .append("'").append(stats.getPlayerName()).append("', ")
                .append("'").append(stats.getMinerExp()).append("', ")
                .append("'").append(stats.getMinerLvl()).append("'");

        // Optional statistic fields
        for (final NamespacedKey entry : stats.getStatisticKeys()) {
            if (entry == null) continue;

            fields.append(", `").append(entry.getKey()).append("`");
            int dropStatistic = stats.getStatistic(entry);
            values.append(", '").append(dropStatistic).append("'");
        }

        query.append(fields).append(") VALUES (").append(values).append(");");

        return DatabaseManager.queryUpdate(getHikariDataSource(), query.toString());
    }

    public int loadPlayerStats() {
        final String queryStatement = "SELECT * FROM " + "`" + DatabaseManager.TABLE_PLAYER_STATS + "`";

        final PlayersData playerSetup = plugin.getPlayersData();

        try (final Connection conn = getHikariDataSource().getConnection();
             final PreparedStatement ps = conn.prepareStatement(queryStatement);
             final ResultSet result = ps.executeQuery()) {

            if (result == null) {
                new Message("Couldn't load Personal Stone Stats on start!").log(Level.SEVERE);
                return -1;
            }

            int loadCount = 0;
            while (result.next()) {
                final ResultSetMetaData metaData = result.getMetaData();
                final UUID uuid = UUID.fromString(result.getString("PlayerUUID"));
                final long minerExp = result.getLong("MinerExp");
                final int minerLvl = result.getInt("MinerLvl");

                final PlayerStats stats = playerSetup.getPlayerStoneMachineStats(uuid);
                stats.setMinerExp(minerExp, false);
                stats.setMinerLvl(minerLvl, false);

                //new Message()"Loading drop stats for " + result.getString("PlayerUUID"));

                final int columnCount = metaData.getColumnCount();
                for (int i = 1; i <= columnCount; i++) {
                    final String columnName = metaData.getColumnName(i);
                    if (columnName.contentEquals("PlayerUUID") || columnName.contentEquals("PlayerName"))
                        continue;
                    else if (columnName.contentEquals("MinerExp") || columnName.contentEquals("MinerLvl"))
                        continue;

                    //column name is expected to be fully qualified (with drop_ or resource_ prefix)
                    final NamespacedKey key = new NamespacedKey(plugin, columnName);
//                    new Message("Set statistic: " + key.getKey() + "=" + result.getInt(columnName)).log(Level.INFO);
                    stats.setStatistic(key, result.getInt(columnName));
                }

                stats.markUnsaved(false);

                loadCount++;
            }

            return loadCount;
        } catch (SQLException ex) {
            new Message("Couldn't query results!").log(Level.SEVERE);
            //noinspection CallToPrintStackTrace
            ex.printStackTrace();
        }

        return 0;
    }

    public int loadPlayerConfig() {
        final String queryStatement = "SELECT * FROM " + "`" + DatabaseManager.TABLE_PLAYER_CONFIG + "`";

        final PlayersData playerSetup = plugin.getPlayersData();

        try (final Connection conn = getHikariDataSource().getConnection();
             final PreparedStatement ps = conn.prepareStatement(queryStatement);
             final ResultSet result = ps.executeQuery()) {

            if (result == null) {
                new Message("Couldn't load Personal Stone Stats on start!").log(Level.SEVERE);
                return -1;
            }

            int loadCount = 0;
            while (result.next()) {
                final ResultSetMetaData metaData = result.getMetaData();
                final UUID uuid = UUID.fromString(result.getString("PlayerUUID"));
                final PlayerConfig config = playerSetup.getPersonalDropConfig(uuid);

                //new Message()"Loading drop configuration for " + result.getString("PlayerUUID"));

                final int columnCount = metaData.getColumnCount();
                for (int i = 1; i <= columnCount; i++) {
                    final String columnName = metaData.getColumnName(i);
                    if (columnName.contentEquals("PlayerUUID") || columnName.contentEquals("PlayerName"))
                        continue;

                    //expected column name is fully qualified, like: drop_diamond, drop_gold, resource_stone
                    final NamespacedKey key = new NamespacedKey(plugin, columnName);
                    config.setDropEntry(key, result.getBoolean(columnName));
                }

                config.markUnsaved(false);

                loadCount++;
            }

            return loadCount;
        } catch (SQLException e) {
            new Message("Couldn't query results!").log(Level.SEVERE);
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }

        return 0;
    }

    public void insertDropMultiplierRecord(String callerName, @NotNull UUID callerUniqueId, float value, long start, long end) {
        final Timestamp startTime = new Timestamp(start);
        final Timestamp timeoutTime = new Timestamp(end);

        String query = "INSERT INTO " + "`" + DatabaseManager.TABLE_DROP_MULTIPLIER + "` " +
                " (`MultiplierId`, `SetOn`, `Timeout`, `MultiplierValue`, `CallerName`, `CallerUUID`) VALUES (NULL," +
                " '" + startTime + "'," +
                " '" + timeoutTime + "'," +
                " '" + value + "', " +
                " '" + callerName + "', " +
                " '" + callerUniqueId + "');";

        DatabaseManager.queryUpdate(getHikariDataSource(), query);
    }

    public void readPreviousMultiplierFromDatabase(final DropMultiplier multiplier) {
        String query = "SELECT * FROM `" + DatabaseManager.TABLE_DROP_MULTIPLIER + "` " +
                "ORDER BY `" + DatabaseManager.TABLE_DROP_MULTIPLIER + "`.`Timeout` DESC " +
                "LIMIT 1;";

        try (final Connection conn = getHikariDataSource().getConnection();
             final PreparedStatement ps = conn.prepareStatement(query);
             final ResultSet response = ps.executeQuery()) {

            if (response == null) {
                new Message("Couldn't recover drop multiplier from database!").log(Level.WARNING);
                return;
            }

            while (response.next()) {
                final Timestamp startTime = response.getTimestamp("SetOn");
                final Timestamp timeoutTime = response.getTimestamp("Timeout");
                final float multiplierValue = response.getFloat("MultiplierValue");
                final String callerName = response.getString("CallerName");
                final String callerUUID = response.getString("CallerUUID");

                multiplier.setDropMultiplier(callerName, UUID.fromString(callerUUID), multiplierValue, timeoutTime, startTime);
            }
        } catch (SQLException ex) {
            //noinspection CallToPrintStackTrace
            ex.printStackTrace();
        }
    }

    private void addTableColumnIfNotExist(final String tableName, final String columnName, final String columnType, final String defaultValue) {
        try (Connection conn = getHikariDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement("PRAGMA table_info(" + tableName + ");");
             ResultSet rs = stmt.executeQuery()) {

            boolean columnExists = false;
            while (rs.next()) {
                String existingColumnName = rs.getString("name");
                if (existingColumnName.equalsIgnoreCase(columnName)) {
                    columnExists = true;
                    break;
                }
            }

            if (!columnExists) {
                DatabaseManager.queryUpdate(getHikariDataSource(),
                        "ALTER TABLE `" + tableName + "` ADD COLUMN `" + columnName + "` " + columnType +
                                " NOT NULL DEFAULT " + defaultValue);
            }
        } catch (SQLException e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
    }

    private void makeDropMultiplierTable() {
        String query = "CREATE TABLE IF NOT EXISTS `" + DatabaseManager.TABLE_DROP_MULTIPLIER + "`" +
                " (" +
                " `MultiplierId` INTEGER PRIMARY KEY," +
                " `SetOn` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                " `Timeout` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                " `MultiplierValue` REAL NOT NULL DEFAULT '1.0'," +
                " `CallerName` TEXT," +
                " `CallerUUID` TEXT" +
                ") ";

        DatabaseManager.queryUpdate(getHikariDataSource(), query);
    }

    private void makePlayerConfigTable() {
        String query = "CREATE TABLE IF NOT EXISTS " + DatabaseManager.TABLE_PLAYER_CONFIG +
                " (" +
                " `PlayerUUID` TEXT PRIMARY KEY," +
                " `PlayerName` TEXT" +
                ") ";

        DatabaseManager.queryUpdate(getHikariDataSource(), query);
    }

    private void makePlayerStatsTable() {
        String query = "CREATE TABLE IF NOT EXISTS " + DatabaseManager.TABLE_PLAYER_STATS +
                " (" +
                " `PlayerUUID` TEXT PRIMARY KEY," +
                " `PlayerName` TEXT," +
                " `MinerExp` BIGINT UNSIGNED NOT NULL DEFAULT '0'," +
                " `MinerLvl` INT UNSIGNED NOT NULL DEFAULT '1'" +
                ") ";

        DatabaseManager.queryUpdate(getHikariDataSource(), query);
    }
}
