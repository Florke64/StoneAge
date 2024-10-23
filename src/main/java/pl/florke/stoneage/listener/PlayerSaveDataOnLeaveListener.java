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

package pl.florke.stoneage.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import pl.florke.stoneage.StoneAge;
import pl.florke.stoneage.database.playerdata.PlayerConfig;
import pl.florke.stoneage.database.playerdata.PlayerStats;
import pl.florke.stoneage.util.Message;

import java.util.UUID;
import java.util.logging.Level;

public class PlayerSaveDataOnLeaveListener implements Listener {

    private final StoneAge plugin;

    public PlayerSaveDataOnLeaveListener() {
        plugin = StoneAge.getPlugin(StoneAge.class);
    }

    @EventHandler
    public void onPlayerQuit(@NotNull PlayerQuitEvent event) {
        final UUID playerUUID = event.getPlayer().getUniqueId();
        final PlayerConfig dropConfig = plugin.getPlayersData().getPersonalDropConfig(playerUUID);
        final PlayerStats dropStats = plugin.getPlayersData().getPlayerStoneMachineStats(playerUUID);

        saveDropConfig(dropConfig);
        saveDropStatistics(dropStats);
    }

    private void saveDropConfig(@NotNull PlayerConfig dropConfig) {
        if (!dropConfig.hasUnsavedEdits()) {
            return;
        }

        new BukkitRunnable() {

            @Override
            public void run() {
                final int response = plugin.getPlayersData().savePersonalDropConfigInDatabase(dropConfig);

                if (response == 0) {
                    new Message("Database executeUpdate() successful but responded with 0.").log(Level.WARNING);
                }

                new Message("Saved Personal Configuration for " + dropConfig.getUniqueId() + ".").log(Level.INFO);
            }
        }.runTaskAsynchronously(plugin);
    }

    private void saveDropStatistics(@NotNull PlayerStats dropStats) {
        if (!dropStats.hasUnsavedEdits()) {
            return;
        }

        new BukkitRunnable() {

            @Override
            public void run() {
                final int response = plugin.getPlayersData().savePersonalStoneStatsInDatabase(dropStats);

                if (response == 0) {
                    new Message("Database executeUpdate() successful but responded with 0.").log(Level.WARNING);
                }

                new Message("Saved Personal Drop Stats for " + dropStats.getUniqueId() + ".").log(Level.INFO);
            }
        }.runTaskAsynchronously(plugin);
    }

}
