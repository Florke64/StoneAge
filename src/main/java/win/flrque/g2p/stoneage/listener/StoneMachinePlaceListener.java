/*
 * Copyright Go2Play.pl (c) 2020.
 * Program made for Go2Play Skyblock server. It's not allowed to re-distribute the code.
 * Author: FlrQue
 */

package win.flrque.g2p.stoneage.listener;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Dispenser;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.material.Directional;
import org.jetbrains.annotations.NotNull;
import win.flrque.g2p.stoneage.StoneAge;

public class StoneMachinePlaceListener implements Listener {

    private final StoneAge plugin;

    public StoneMachinePlaceListener() {
        plugin = StoneAge.getPlugin(StoneAge.class);
    }

    @EventHandler
    public void onStoneMachinePlace(@NotNull BlockPlaceEvent event) {
        if(event.isCancelled())
            return;

        final Block placedBlock = event.getBlockPlaced();

        if(!plugin.getStoneMachine().isStoneMachine(event.getBlock()))
            return;

        final Directional machine = (Directional) placedBlock.getState().getData();
        final Location stoneGenerationLocation = placedBlock.getRelative(machine.getFacing(), 1).getLocation();

        final Dispenser stoneMachine = (Dispenser) placedBlock.getState();
        stoneMachine.getInventory().setItem(0, plugin.getStoneMachine().getMachineLabel());

        plugin.getStoneMachine().generateStone(stoneGenerationLocation);
    }

}
