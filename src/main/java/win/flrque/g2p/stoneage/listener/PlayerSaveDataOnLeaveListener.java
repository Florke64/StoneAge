/*
 * Copyright Go2Play.pl (c) 2020.
 * Program made for Go2Play Skyblock server. It's not allowed to re-distribute the code.
 * Author: FlrQue
 */

package win.flrque.g2p.stoneage.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import win.flrque.g2p.stoneage.StoneAge;
import win.flrque.g2p.stoneage.database.playerdata.PlayerConfig;
import win.flrque.g2p.stoneage.database.playerdata.PlayerStats;

import java.sql.SQLException;
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
        final PlayerConfig dropConfig = plugin.getPlayerSetup().getPersonalDropConfig(playerUUID);
        final PlayerStats dropStats = plugin.getPlayerSetup().getPlayerStoneMachineStats(playerUUID);

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
                final int response = plugin.getPlayerSetup().savePersonalDropConfigInDatabase(dropConfig);

                if (response == 0) {
                    plugin.getLogger().log(Level.WARNING, "Database executeUpdate() successful but responded with 0.");
                }

                plugin.getLogger().log(Level.INFO, "Saved Personal Configuration for " + dropConfig.getUniqueId() + ".");
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
                final int response = plugin.getPlayerSetup().savePersonalStoneStatsInDatabase(dropStats);

                if (response == 0) {
                    plugin.getLogger().log(Level.WARNING, "Database executeUpdate() successful but responded with 0.");
                }

                plugin.getLogger().log(Level.INFO, "Saved Personal Drop Stats for " + dropStats.getUniqueId() + ".");
            }
        }.runTaskAsynchronously(plugin);
    }

}
