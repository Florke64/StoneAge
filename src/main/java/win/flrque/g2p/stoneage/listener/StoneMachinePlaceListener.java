package win.flrque.g2p.stoneage.listener;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.material.Directional;
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
        final Block placedBlock = event.getBlockPlaced();

        if(!plugin.getStoneMachine().isStoneMachine(event.getBlock()))
            return;

        final Directional machine = (Directional) placedBlock.getState().getData();
        final Location stoneGenerationLocation = placedBlock.getRelative(machine.getFacing(), 1).getLocation();

        plugin.getStoneMachine().generateStone(stoneGenerationLocation);
    }

}
