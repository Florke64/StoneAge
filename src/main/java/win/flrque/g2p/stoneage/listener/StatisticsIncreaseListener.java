/*
 * Copyright Go2Play.pl (c) 2020.
 * Program made for Go2Play Skyblock server. It's not allowed to re-distribute the code.
 * Author: FlrQue
 */

package win.flrque.g2p.stoneage.listener;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import win.flrque.g2p.stoneage.StoneAge;
import win.flrque.g2p.stoneage.database.playerdata.PlayerStats;
import win.flrque.g2p.stoneage.drop.DropEntry;
import win.flrque.g2p.stoneage.drop.DropLoot;
import win.flrque.g2p.stoneage.event.StoneMachineStoneBreakEvent;

import java.util.Collection;
import java.util.UUID;

public class StatisticsIncreaseListener implements Listener {

    private final StoneAge plugin;

    public StatisticsIncreaseListener() {
        this.plugin = StoneAge.getPlugin(StoneAge.class);
    }

    @EventHandler
    public void onStoneMachineUse(@NotNull StoneMachineStoneBreakEvent event) {
        final Player player = event.getPlayer();

        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) {
            return;
        }

        final UUID playerUUID = player.getUniqueId();
        final PlayerStats playerStats = plugin.getPlayerSetup().getPlayerStoneMachineStats(playerUUID);

        final DropLoot dropLoot = event.getLoot();
        if (dropLoot == null) {
            return;
        }

        final Collection<DropEntry> dropEntries = dropLoot.getActiveDropEntries();

        for (DropEntry entry : dropEntries) {
            final int amount = event.getLoot().getAmountLooted(entry);
            playerStats.increaseStatistic(entry.getEntryName(), amount);
        }
    }

}
