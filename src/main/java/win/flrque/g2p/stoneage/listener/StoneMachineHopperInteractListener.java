/*
 * Copyright Go2Play.pl (c) 2020.
 * Program made for Go2Play Skyblock server. It's not allowed to re-distribute the code.
 * Author: FlrQue
 */

package win.flrque.g2p.stoneage.listener;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import win.flrque.g2p.stoneage.StoneAge;
import win.flrque.g2p.stoneage.machine.ItemAutoSmelter;
import win.flrque.g2p.stoneage.machine.StoneMachine;

public class StoneMachineHopperInteractListener implements Listener {

    private final StoneAge plugin;
    private final StoneMachine stoneMachine;
    private final ItemAutoSmelter autoSmelter;

    public StoneMachineHopperInteractListener() {
        plugin = StoneAge.getPlugin(StoneAge.class);

        this.stoneMachine = plugin.getStoneMachine();
        this.autoSmelter = plugin.getStoneMachine().getItemSmelter();
    }

    @EventHandler
    public void onHopperInteraction(@NotNull InventoryMoveItemEvent event) {
        if (event.isCancelled())
            return;

        final Inventory sourceInventory = event.getSource();
        final Inventory destinationInventory = event.getDestination();

        //Blocking hopper output
        if (plugin.getStoneMachine().isStoneMachine(sourceInventory)) {
//            plugin.getLogger().log(Level.INFO, "InventoryMoveItemEvent: source inventory is a stone machine!");
            event.setCancelled(true);
        }

        if (plugin.getStoneMachine().isStoneMachine(destinationInventory)) {
//            plugin.getLogger().log(Level.INFO, "InventoryMoveItemEvent: destination inventory is a stone machine!");
            final ItemStack fuelItem = event.getItem();
            if (stoneMachine.isHopperInputAllowed() && fuelItem.getType() == Material.COAL) {
                final int fuelAmount = fuelItem.getAmount() * 8;

                final boolean success = autoSmelter.addAutoSmeltingUse(destinationInventory, fuelAmount);

                if (success) event.setItem(new ItemStack(Material.AIR, 1));

                return;
            }

            event.setCancelled(true);
        }

    }

}
