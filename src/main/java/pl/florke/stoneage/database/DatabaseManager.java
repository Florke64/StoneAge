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

import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import pl.florke.stoneage.StoneAge;
import pl.florke.stoneage.config.DatabaseConfigReader;
import pl.florke.stoneage.database.playerdata.PlayerConfig;
import pl.florke.stoneage.database.playerdata.PlayerStats;
import pl.florke.stoneage.database.wrapper.DatabaseWrapper;
import pl.florke.stoneage.database.wrapper.MySQLWrapper;
import pl.florke.stoneage.database.wrapper.SQLiteWrapper;
import pl.florke.stoneage.util.Message;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;
import java.util.logging.Level;

public class DatabaseManager {

    public static final String TABLE_PLAYER_STATS = "stoneage_stats";
    public static final String TABLE_PLAYER_DROP_CONFIG = "stoneage_config";
    public static final String TABLE_DROP_MULTIPLIER = "stoneage_multiplier";

    private final DatabaseConfigReader databaseConfig;
    private final DatabaseWrapper databaseWrapper;

    private BukkitRunnable autosaveRunnable;

    public DatabaseManager(DatabaseConfigReader databaseConfig) {
        this.databaseConfig = databaseConfig;
        this.databaseWrapper = createSQLWrapper();
    }

    public void initAsyncAutosave(final long period) {
        new Message("Initialized Async Autosave.").log(Level.INFO);

        final StoneAge plugin = StoneAge.getPlugin(StoneAge.class);
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

    public void loadAllPlayers() {

        new BukkitRunnable() {
            @Override
            public void run() {
                final long dbLoadStartTime = System.currentTimeMillis();

                //Loading data of all players from database
                final int statsCount = getSQLWrapper().loadPersonalStoneStatsFromDatabase();
                final int configsCount = getSQLWrapper().loadPersonalDropConfigFromDatabase();

                final long dbLoadFinishTime = System.currentTimeMillis();

                final Message success = new Message();
                success.addLines("Loaded PlayerStats ($_1) and PlayerConfigs ($_2) from the database");
                success.addLines("Loading took $_3ms");
                success.placeholder(1, Integer.toString(statsCount));
                success.placeholder(2, Integer.toString(configsCount));
                success.placeholder(3, Long.toString(dbLoadFinishTime - dbLoadStartTime));
                success.log(Level.INFO);

            }
        }.runTaskAsynchronously(StoneAge.getPlugin(StoneAge.class));
    }

    public DatabaseWrapper createSQLWrapper() {
        return databaseConfig.getSQLWrapperType().equals("mysql") ?
                new MySQLWrapper(databaseConfig) : new SQLiteWrapper(databaseConfig);
    }

    public static int queryUpdate(final @NotNull HikariDataSource connectionPool, final @NotNull String query) {
        new Message("Query: " + query).log(Level.INFO);

        try (final Connection conn = connectionPool.getConnection();
             final PreparedStatement ps = conn.prepareStatement(query)) {

            if (ps == null) return -1;

            final int response = ps.executeUpdate();

            if (!ps.isClosed()) {
                ps.close();
            }

            return response;
        } catch (SQLException ex) {
            //Checks the error code and skipping exception's stack trace printing
            if (ex.getErrorCode() != 1060) {
                new Message(ex.getMessage()).log(Level.WARNING);
            }
        }

        return -1;
    }

    public int saveAllOnlinePlayersData() {
        final StoneAge plugin = StoneAge.getPlugin(StoneAge.class);

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

    public void onDisable() {
        if (autosaveRunnable != null)
            autosaveRunnable.cancel();

        databaseWrapper.onDisable();
    }

    public DatabaseWrapper getSQLWrapper() {
        return databaseWrapper;
    }

}
