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
import pl.florke.stoneage.drop.DropEntry;
import pl.florke.stoneage.drop.DropMultiplier;
import pl.florke.stoneage.util.Message;

import java.sql.*;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

@SuppressWarnings("CallToPrintStackTrace")
public class MySQLWrapper extends DatabaseWrapper {

    public MySQLWrapper(@NotNull DatabaseConfigReader databaseConfig) {
        super(databaseConfig);
        initDatabase();
    }

    private void initDatabase() {
        makePlayerStatsTable();
        makePlayerDropConfigTable();
        makeDropMultiplierTable();

        for (DropEntry entry : StoneAge.getPlugin(StoneAge.class).getDropCalculator().getDropEntries()) {
            final String dropEntryName = entry.getEntryId();
            addTableColumnIfNotExist(DatabaseManager.TABLE_PLAYER_STATS, dropEntryName, "INT", "0");
            addTableColumnIfNotExist(DatabaseManager.TABLE_PLAYER_DROP_CONFIG, dropEntryName, "BOOLEAN", "true");
        }

        addTableColumnIfNotExist(DatabaseManager.TABLE_PLAYER_STATS, "primitive_drop", "INT", "0");
        addTableColumnIfNotExist(DatabaseManager.TABLE_PLAYER_DROP_CONFIG, "primitive_drop", "BOOLEAN", "true");
    }

    /**
     * Connects to the specified MySQL server and creates the specified database if it doesn't exist yet.
     * This method is connecting to database server without specifying database name in contrast to Hikari.
     *
     * @param databaseConfig the configuration for the database connection
     */
    private void createDatabaseIfNotExists(@NotNull DatabaseConfigReader databaseConfig) {
        String jdbcUrl = "jdbc:mysql://" + databaseConfig.getServerAddress() + ":" + databaseConfig.getServerPort();
        String createDatabaseQuery = "CREATE DATABASE IF NOT EXISTS " + databaseConfig.getDatabaseName();

        try (Connection conn = DriverManager.getConnection(jdbcUrl, databaseConfig.getUsername(), databaseConfig.getPassword());
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(createDatabaseQuery);
            new Message("Database created or already exists: " + databaseConfig.getDatabaseName()).log(Level.INFO);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    protected HikariConfig getHikariConfig(@NotNull DatabaseConfigReader databaseConfig) {
        /* This method shall be called before first call to setupConnectionPool.
         * Setup connection pool relies on an existing database. This is specific to MySQL. */
        if (getHikariDataSource() == null)
            createDatabaseIfNotExists(databaseConfig);

        final HikariConfig config = new HikariConfig();
        config.setPoolName("StoneAgeDatabasePool");

        final String hostname = databaseConfig.getServerAddress();
        final int port = databaseConfig.getServerPort();
        final String username = databaseConfig.getUsername();
        final String password = databaseConfig.getPassword();
        final String database = databaseConfig.getDatabaseName();


        config.setJdbcUrl("jdbc:mysql://" + hostname + ":" + port + "/" + database);

        config.setUsername(username);
        config.setPassword(password);

        return config;
    }

    public int runUpdateForPersonalDropConfig(@NotNull PlayerConfig config) {

        final StringBuilder query = new StringBuilder();
        final StringBuilder fields = new StringBuilder();
        final StringBuilder values = new StringBuilder();
        final StringBuilder keyDuplicate = new StringBuilder();

        // Init query
        query.append("INSERT INTO ")
                // "database_name.`table_name` ("
                .append(getDatabaseName()).append(".`").append(DatabaseManager.TABLE_PLAYER_DROP_CONFIG).append("` (");

        // Obligatory fields
        fields.append("`PlayerUUID`, "); // appends PlayerUUID field
        fields.append("`PlayerName`, "); // appends PlayerName field

        final Set<DropEntry> entries = config.getCustomDropEntries();

        // appends query details for each DropEntry
        for (Iterator<DropEntry> it = entries.iterator(); it.hasNext(); ) {
            DropEntry entry = it.next();
            if (entry == null) continue;

            // appends EntryName field to target columns list
            fields.append("`").append(entry.getEntryId()).append("`");

            // appends personal PlayerConfig values
            int dropSwitchStatus = config.isDropping(entry) ? 1 : 0;
            values.append("'").append(dropSwitchStatus).append("'"); // "'1'"

            // defaults
            keyDuplicate.append("`").append(entry.getEntryId()).append("`=VALUES")
                    .append("(`").append(entry.getEntryId()).append("`)");

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

        return DatabaseManager.queryUpdate(getHikariDataSource(), query.toString());
    }

    public int runUpdateForPersonalStoneStats(@NotNull PlayerStats stats) {

        final StringBuilder query = new StringBuilder();
        final StringBuilder fields = new StringBuilder();
        final StringBuilder values = new StringBuilder();
        final StringBuilder keyDuplicate = new StringBuilder();

        // Init query
        query.append("INSERT INTO ")
                // "database_name.`table_name` ("
                .append(getDatabaseName()).append(".`").append(DatabaseManager.TABLE_PLAYER_STATS).append("` (");

        // Obligatory fields
        fields.append("`PlayerUUID`, ");
        fields.append("`PlayerName`, ");
        fields.append("`MinerExp`, ");
        fields.append("`MinerLvl`, ");

        for (final Iterator<NamespacedKey> it = stats.getStatisticKeys().iterator(); it.hasNext(); ) {
            final NamespacedKey entry = it.next();
            if (entry == null) continue;

            fields.append("`").append(entry.getKey()).append("`"); // "`stone`"

            int dropStatistic = stats.getStatistic(entry);
            values.append("'").append(dropStatistic).append("'"); // "'1'"

            keyDuplicate.append("`").append(entry.getKey()).append("`=VALUES(`").append(entry.getKey()).append("`)");

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

        return DatabaseManager.queryUpdate(getHikariDataSource(), query.toString());
    }

    public int loadPersonalStoneStatsFromDatabase() {
        final String queryStatement = "SELECT * FROM " + getDatabaseName() + ".`" + DatabaseManager.TABLE_PLAYER_STATS + "`";

        final PlayersData playerSetup = StoneAge.getPlugin(StoneAge.class).getPlayersData();

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

                    //expected column name is fully qualified, like: drop_diamond, drop_gold, primitive_stone
                    final NamespacedKey key = new NamespacedKey(StoneAge.getPlugin(StoneAge.class), columnName);
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

    public int loadPersonalDropConfigFromDatabase() {
        final String queryStatement = "SELECT * FROM " + getDatabaseName() + ".`" + DatabaseManager.TABLE_PLAYER_DROP_CONFIG + "`";

        final PlayersData playerSetup = StoneAge.getPlugin(StoneAge.class).getPlayersData();

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

                    //expected column name is fully qualified, like: drop_diamond, drop_gold, primitive_stone
                    final NamespacedKey key = new NamespacedKey(StoneAge.getPlugin(StoneAge.class), columnName);
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

        String query = "INSERT INTO " + getDatabaseName() + ".`" + DatabaseManager.TABLE_DROP_MULTIPLIER + "` " +
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
        DatabaseManager.queryUpdate(getHikariDataSource(), "ALTER TABLE " + getDatabaseName() + ".`" + tableName + "` " +
                "ADD COLUMN `" + columnName + "` " + columnType + " NOT NULL default " + defaultValue);
    }

    private void makeDropMultiplierTable() {
        String query = "CREATE TABLE IF NOT EXISTS `" + getDatabaseName() + "`.`" + DatabaseManager.TABLE_DROP_MULTIPLIER + "`" +
                " (" +
                " `MultiplierId` INT NOT NULL AUTO_INCREMENT," +
                " `SetOn` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                " `Timeout` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                " `MultiplierValue` FLOAT NOT NULL DEFAULT '1.0'," +
                " `CallerName` VARCHAR(16)," +
                " `CallerUUID` VARCHAR(36)," +
                " PRIMARY KEY (`MultiplierId`)" +
                ") ";

        DatabaseManager.queryUpdate(getHikariDataSource(), query);
    }

    private void makePlayerDropConfigTable() {
        String query = "CREATE TABLE IF NOT EXISTS " + getDatabaseName() + "." + DatabaseManager.TABLE_PLAYER_DROP_CONFIG +
                " (" +
                " `PlayerUUID` VARCHAR(36)," +
                " `PlayerName` VARCHAR(16)," +
                " PRIMARY KEY (`PlayerUUID`)" +
                ") ";

        DatabaseManager.queryUpdate(getHikariDataSource(), query);
    }

    private void makePlayerStatsTable() {
        String query = "CREATE TABLE IF NOT EXISTS " + getDatabaseName() + "." + DatabaseManager.TABLE_PLAYER_STATS +
                " (" +
                " `PlayerUUID` VARCHAR(36)," +
                " `PlayerName` VARCHAR(16)," +
                " `MinerExp` BIGINT UNSIGNED NOT NULL DEFAULT '0'," +
                " `MinerLvl` INT UNSIGNED NOT NULL DEFAULT '1'," +
                " PRIMARY KEY (`PlayerUUID`)" +
                ") ";

        DatabaseManager.queryUpdate(getHikariDataSource(), query);
    }

}