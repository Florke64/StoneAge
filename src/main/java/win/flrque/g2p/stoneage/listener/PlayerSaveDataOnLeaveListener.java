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
import win.flrque.g2p.stoneage.database.playerdata.PersonalDropConfig;
import win.flrque.g2p.stoneage.database.playerdata.StoneMachinePlayerStats;

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
        final PersonalDropConfig dropConfig = plugin.getPlayerSetup().getPersonalDropConfig(playerUUID);
        final StoneMachinePlayerStats dropStats = plugin.getPlayerSetup().getPlayerStoneMachineStats(playerUUID);

        if(!dropConfig.hasUnsavedEdits()) {
            return;
        }

        new BukkitRunnable() {

            @Override
            public void run() {
                plugin.getPlayerSetup().savePersonalDropConfigInDatabase(dropConfig);
                plugin.getLogger().log(Level.INFO, "Saved Personal Configuration for " + playerUUID + ".");
                plugin.getPlayerSetup().savePersonalStoneStatsInDatabase(dropStats);
                plugin.getLogger().log(Level.INFO, "Saved Personal Drop Stats for " + playerUUID + ".");

            }
        }.runTaskAsynchronously(plugin);
    }

}
