package win.flrque.g2p.stoneage.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import win.flrque.g2p.stoneage.StoneAge;

public class StoneMachinePlaceListener implements Listener {

    private final StoneAge plugin;

    public StoneMachinePlaceListener() {
        plugin = StoneAge.getPlugin(StoneAge.class);
    }

    @EventHandler
    public void onStoneMachinePlace(BlockPlaceEvent event) {
        if(event.isCancelled())
            return;

        final Player player = event.getPlayer();

        if(!plugin.getStoneMachine().isStoneMachine(event.getBlock()))
            return;

        player.sendMessage("<Edward> Twoja stoniarka postawiona, gz buddy!");
    }

}
