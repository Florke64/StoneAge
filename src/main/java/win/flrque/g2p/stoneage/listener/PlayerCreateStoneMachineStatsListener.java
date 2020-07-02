/*
 * Copyright Go2Play.pl (c) 2020.
 * Program made for Go2Play Skyblock server. It's not allowed to re-distribute the code.
 * Author: FlrQue
 */

package win.flrque.g2p.stoneage.listener;

import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import win.flrque.g2p.stoneage.StoneAge;
import win.flrque.g2p.stoneage.database.playerdata.StoneMachinePlayerStats;

public class PlayerCreateStoneMachineStatsListener implements Listener {

    private final StoneAge plugin;

    public PlayerCreateStoneMachineStatsListener() {
        plugin = StoneAge.getPlugin(StoneAge.class);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        final OfflinePlayer player = event.getPlayer();

        final StoneMachinePlayerStats playerStats = plugin.getPlayerSetup().getPlayerStoneMachineStats(player.getUniqueId());
    }

}
