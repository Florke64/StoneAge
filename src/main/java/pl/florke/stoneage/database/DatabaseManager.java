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

import org.bukkit.scheduler.BukkitRunnable;
import pl.florke.stoneage.StoneAge;
import pl.florke.stoneage.config.DatabaseConfigReader;
import pl.florke.stoneage.database.wrapper.MySQLWrapper;
import pl.florke.stoneage.database.wrapper.SQLWrapper;
import pl.florke.stoneage.util.Message;

import java.util.logging.Level;

public class DatabaseManager {

    public static final String TABLE_PLAYER_STATS = "stoneage_stats";
    public static final String TABLE_PLAYER_DROP_CONFIG = "stoneage_config";
    public static final String TABLE_DROP_MULTIPLIER = "stoneage_multiplier";

    private final DatabaseConfigReader databaseConfig;
    private final SQLWrapper sqlWrapper;

    private BukkitRunnable autosaveRunnable;

    public DatabaseManager(DatabaseConfigReader databaseConfig) {
        this.databaseConfig = databaseConfig;
        this.sqlWrapper = createSQLWrapper();
    }

    public SQLWrapper getSQLWrapper() {
        return sqlWrapper;
    }

    public void initAsyncAutosave(final long period) {
        new Message("Initialized Async Autosave.").log(Level.INFO);

        final StoneAge plugin = StoneAge.getPlugin(StoneAge.class);
        final SQLWrapper sqlWrapper = plugin.getSQLWrapper();
        autosaveRunnable = new BukkitRunnable() {

            @Override
            public void run() {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        final int savedCount = sqlWrapper.saveAllOnlinePlayersData();

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

    @SuppressWarnings("ConditionalExpressionWithIdenticalBranches")
    public SQLWrapper createSQLWrapper() {
        return databaseConfig.getSQLWrapperType().equals("mysql") ?
                new MySQLWrapper(databaseConfig) : new MySQLWrapper(databaseConfig); //new SQLiteWrapper(this);
    }

    public void onDisable() {
        if (autosaveRunnable != null) {
            autosaveRunnable.cancel();
        }

        sqlWrapper.onDisable();
    }

}
