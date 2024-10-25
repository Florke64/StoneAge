/*
 * @Florke64 <Daniel Chojnacki>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package pl.florke.stoneage.listener;

import org.bukkit.Material;
import org.bukkit.block.Dispenser;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import pl.florke.stoneage.StoneAge;
import pl.florke.stoneage.machine.ItemAutoSmelter;
import pl.florke.stoneage.machine.StoneMachine;

public class StoneMachineHopperInteractListener implements Listener {

    private final StoneMachine stoneMachine;
    private final ItemAutoSmelter autoSmelter;

    public StoneMachineHopperInteractListener() {
        StoneAge plugin = StoneAge.getPlugin(StoneAge.class);

        this.stoneMachine = plugin.getStoneMachine();
        this.autoSmelter = plugin.getStoneMachine().getItemSmelter();
    }

    @EventHandler
    public void onHopperInteraction(@NotNull final InventoryMoveItemEvent event) {
        if (event.isCancelled())
            return;

        final Inventory sourceInventory = event.getSource();
        final Inventory destinationInventory = event.getDestination();

        if (!isDispenserInventory(sourceInventory) && !isDispenserInventory(destinationInventory)) {
            return;
        }

        //Blocking hopper output
        if (stoneMachine.isStoneMachine(sourceInventory)) {
//            new Message()"InventoryMoveItemEvent: source inventory is a stone machine!");
            event.setCancelled(true);
        }

        if (stoneMachine.isStoneMachine(destinationInventory)) {
//            new Message()"InventoryMoveItemEvent: destination inventory is a stone machine!");
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

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    // Yes, it just is.
    private boolean isDispenserInventory(@NotNull final Inventory inventoryHolder) {
        return inventoryHolder.getHolder() instanceof Dispenser;
    }

}
