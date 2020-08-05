/*
 * Copyright Go2Play.pl (c) 2020.
 * Program made for Go2Play Skyblock server. It's not allowed to re-distribute the code.
 * Author: FlrQue
 */

package win.flrque.g2p.stoneage.listener;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import win.flrque.g2p.stoneage.StoneAge;
import win.flrque.g2p.stoneage.database.playerdata.PlayerStats;
import win.flrque.g2p.stoneage.event.MinerLevelUpEvent;
import win.flrque.g2p.stoneage.util.Message;

import java.util.UUID;

public class MinerLevelUpListener implements Listener {

    private final StoneAge plugin;

    public MinerLevelUpListener() {
        this.plugin = StoneAge.getPlugin(StoneAge.class);
    }

    @EventHandler
    public void onPlayerLevelUp(MinerLevelUpEvent event) {
        if(event.isCancelled())
            return;

        final PlayerStats stats = event.getPlayerStats();
        final UUID playerUniqueId = stats.getUniqueId();
        final Player player = this.plugin.getServer().getPlayer(playerUniqueId);
        if(player == null || !player.isOnline()) {
            return;
        }

        player.playSound(player.getLocation(), Sound.MUSIC_DRAGON, 1f, 1f);

        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);

        final Message congratulations = new Message("&dGratulacje! &c$_1 &dosiaga &6$_2 &dpoziom gornictwa!");
        congratulations.setVariable(1, player.getName());
        congratulations.setVariable(2, Integer.toString(event.getUpToLevel()));
        congratulations.broadcastToTheServer();
    }
}
