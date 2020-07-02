/*
 * Copyright Go2Play.pl (c) 2020.
 * Program made for Go2Play Skyblock server. It's not allowed to re-distribute the code.
 * Author: FlrQue
 */

package win.flrque.g2p.stoneage.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import win.flrque.g2p.stoneage.StoneAge;

public class DebugGameJoin implements Listener {

    private final StoneAge plugin;

    //TODO: To be deleted...
    public DebugGameJoin() {
        plugin = StoneAge.getPlugin(StoneAge.class);
    }

    @EventHandler
    public void onOperatorJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        if(player == null || !player.isOp())
            return;

        player.sendMessage("<Edward> Jako admin, dostajesz stoniarke do EQ!");
        player.sendMessage("<Edward> P.S. to tylko tymczasowe działanie do celów debugowania.");
        player.sendMessage("PlayerUUID: " + player.getUniqueId());
        player.sendMessage("PlayerUUID.toString(): " + player.getUniqueId().toString());
        player.getInventory().addItem(plugin.getStoneMachine().createStoneMachineItem());
    }

}
