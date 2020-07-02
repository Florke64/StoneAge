/*
 * Copyright Go2Play.pl (c) 2020.
 * Program made for Go2Play Skyblock server. It's not allowed to re-distribute the code.
 * Author: FlrQue
 */

package win.flrque.g2p.stoneage.listener;

import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseEvent;
import win.flrque.g2p.stoneage.StoneAge;

public class StoneMachineRedstoneInteractListener implements Listener {

    private final StoneAge plugin;

    public StoneMachineRedstoneInteractListener() {
        plugin = StoneAge.getPlugin(StoneAge.class);
    }

    @EventHandler
    public void onRedstoneInteract(BlockDispenseEvent event) {
        if(event.isCancelled())
            return;

        final Block interactedBlock = event.getBlock();
        if(!plugin.getStoneMachine().isStoneMachine(interactedBlock))
            return;

        event.setCancelled(true);

        /*This code is ready and provides stone generation on Redstone input
        *   but this can be easily abused and lag a server!
        * TODO: add possibility to disable this functionality with config.yml
        */
//        final Dispenser stoneMachine = (Dispenser) interactedBlock.getState();
//        final Location stoneLocation = plugin.getStoneMachine().getGeneratedStoneLocation(stoneMachine);
//
//        plugin.getStoneMachine().generateStone(stoneLocation, 0);
    }

}
