package win.flrque.g2p.stoneage.listener;

import org.bukkit.block.Dispenser;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import win.flrque.g2p.stoneage.StoneAge;
import win.flrque.g2p.stoneage.gui.StoneMachineWindow;
import win.flrque.g2p.stoneage.gui.Window;

public class StoneMachineInteractListener implements Listener {

    private final StoneAge plugin;

    public StoneMachineInteractListener() {
        plugin = StoneAge.getPlugin(StoneAge.class);
    }

    @EventHandler
    public void onStoneMachineClick(PlayerInteractEvent event) {
        if(event.getPlayer() == null || event.isCancelled())
            return;

        final Player player = event.getPlayer();

        if(!event.getAction().equals(Action.RIGHT_CLICK_BLOCK) || player.isSneaking())
            return;

        if(!plugin.getStoneMachine().isStoneMachine(event.getClickedBlock()))
            return;

        event.setCancelled(true);

        final Window window = new StoneMachineWindow(player, (Dispenser) event.getClickedBlock().getState());
        window.open(player);

    }

}
