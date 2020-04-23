package win.flrque.g2p.stoneage.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import win.flrque.g2p.stoneage.item.StoneMachine;

public class DebugGameJoin implements Listener {

    @EventHandler
    public void onOperatorJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        if(player == null || !player.isOp())
            return;

        player.sendMessage("<Edward> Jako admin, dostajesz stoniarke do EQ!");
        player.sendMessage("<Edward> P.S. to tylko tymczasowe działanie do celów debugowania.");
        player.getInventory().addItem(StoneMachine.getStoneMachineItem());
    }

}
