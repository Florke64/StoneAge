/*
 * Copyright Go2Play.pl (c) 2020.
 * Program made for Go2Play Skyblock server. It's not allowed to re-distribute the code.
 * Author: FlrQue
 */

package win.flrque.g2p.stoneage.listener;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import win.flrque.g2p.stoneage.StoneAge;
import win.flrque.g2p.stoneage.drop.DropEntry;
import win.flrque.g2p.stoneage.drop.PersonalDropConfig;

public class PlayerCreatePersonalConfigListener implements Listener {

    private final StoneAge plugin;

    public PlayerCreatePersonalConfigListener() {
        plugin = StoneAge.getPlugin(StoneAge.class);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        final OfflinePlayer player = event.getPlayer();

        final PersonalDropConfig personalDropConfig = plugin.getDropCalculator().getPersonalDropConfig(player);

        for(DropEntry entry : plugin.getDropCalculator().getDropEntries()) {
            if(player.isOnline()) {
                Bukkit.broadcastMessage(
                entry.getDropEntryIcon().getType() + ": " + plugin.getDropCalculator().getPersonalDropConfig(player).isDropping(entry));

            }
        }
    }

}
