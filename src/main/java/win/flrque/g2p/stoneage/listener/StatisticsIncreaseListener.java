/*
 * Copyright Go2Play.pl (c) 2020.
 * Program made for Go2Play Skyblock server. It's not allowed to re-distribute the code.
 * Author: FlrQue
 */

package win.flrque.g2p.stoneage.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import win.flrque.g2p.stoneage.StoneAge;
import win.flrque.g2p.stoneage.database.playerdata.StoneMachinePlayerStats;
import win.flrque.g2p.stoneage.drop.DropEntry;
import win.flrque.g2p.stoneage.event.StoneMachineStoneBreakEvent;

import java.util.Collection;
import java.util.UUID;

public class StatisticsIncreaseListener implements Listener {

    private final StoneAge plugin;

    public StatisticsIncreaseListener() {
        this.plugin = StoneAge.getPlugin(StoneAge.class);
    }

    @EventHandler
    public void onStoneMachineUse(StoneMachineStoneBreakEvent event) {
        final UUID playerUUID = event.getPlayer().getUniqueId();
        final StoneMachinePlayerStats playerStats = plugin.getPlayerSetup().getPlayerStoneMachineStats(playerUUID);

        final Collection<DropEntry> dropEntries = event.getLoot().getActiveDropEntries();

        for(DropEntry entry : dropEntries) {
            playerStats.increaseStatistic(entry.getEntryName());
        }
    }

}
