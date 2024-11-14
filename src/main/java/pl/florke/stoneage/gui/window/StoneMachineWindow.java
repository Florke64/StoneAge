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

package pl.florke.stoneage.gui.window;

import org.bukkit.block.TileState;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryType;
import org.jetbrains.annotations.NotNull;
import pl.florke.stoneage.StoneAge;
import pl.florke.stoneage.gui.InventoryPoint;
import pl.florke.stoneage.gui.ItemButtonFactory;
import pl.florke.stoneage.gui.ItemButtonFactory.ItemButtonType;
import pl.florke.stoneage.gui.Window;
import pl.florke.stoneage.machine.ItemAutoSmelter;
import pl.florke.stoneage.util.Message;

public class StoneMachineWindow extends Window {

    private final TileState machineState;

    public StoneMachineWindow(Player owner, TileState machineState) {
        super(Message.colors(StoneAge.getPlugin(StoneAge.class)
                .getLanguage("stone-drop-actions-title")));

        this.machineState = machineState;
    }

    public TileState getMachineState() {
        return machineState;
    }

    @Override
    public boolean open(@NotNull Player player) {
        if (!super.open(player)) {
            new Message(plugin.getLanguage("stone-machine-gui-error")).send(player);
            return false;
        }

        plugin.getWindowManager().cacheMachine(machineState, this);
        player.openInventory(inventory);

        return true;
    }

    @Override
    public void updateInventoryContent() {
        final ItemButtonFactory buttonFactory = new ItemButtonFactory();

        inventory.setItem(new InventoryPoint(InventoryType.CHEST, 0, 0).getSlotNumber(), buttonFactory.getButton(ItemButtonType.DROP_FILTER));
//        inventory.setItem(new InventoryPoint(InventoryType.CHEST, 0,1).getSlotNumber(), buttonFactory.getButton(ItemButtonType.DROP_INFO));

        inventory.setItem(new InventoryPoint(InventoryType.CHEST, 4, 0).getSlotNumber(), buttonFactory.getButton(ItemButtonType.DROP_MULTIPLIER));
        inventory.setItem(new InventoryPoint(InventoryType.CHEST, 8, 0).getSlotNumber(), buttonFactory.getButton(ItemButtonType.SMELTING_STATUS));
        inventory.setItem(new InventoryPoint(InventoryType.CHEST, 4, 2).getSlotNumber(), buttonFactory.getButton(ItemButtonType.MACHINE_REPAIR));

//        inventory.setItem(new InventoryPoint(InventoryType.CHEST, 8,0).getSlotNumber(), buttonFactory.getButton(ItemButtonType.MACHINE_UPGRADE));

        inventory.setItem(new InventoryPoint(InventoryType.CHEST, 7, 2).getSlotNumber(), buttonFactory.getButton(ItemButtonType.DROP_STATISTICS));
        inventory.setItem(new InventoryPoint(InventoryType.CHEST, 8, 2).getSlotNumber(), buttonFactory.getButton(ItemButtonType.MACHINE_INFO));
    }

    @Override
    public void onClick(ClickType clickType, Player player, @NotNull InventoryPoint clickedPoint) {
//        if(clickType.equals(ClickType.LEFT)) {
//            player.sendMessage(clickedPoint.toString());
//        }

        //Drop info / selector
        if (clickedPoint.getSlotNumber() == 0) {
            player.closeInventory();
            player.performCommand("drop");
        }

        //Automatic smelting info
        if (clickedPoint.getSlotNumber() == 8) {
            player.closeInventory();

            final ItemAutoSmelter autoSmelter = plugin.getStoneMachine().getItemSmelter();
            final int usesLeft = autoSmelter.getAutoSmeltingUsesLeft(machineState);

            final Message msg = new Message();
            if (usesLeft > 0) {
                msg.addLines(plugin.getLanguage("stone-smelting-use"));
                msg.placeholder(1, Integer.toString(usesLeft));
            } else {
                msg.addLines(plugin.getLanguage("stone-smelting-hopper"));
            }

            msg.send(player);
        }

        //Repair Stone Machine
        else if (clickedPoint.getSlotNumber() == 22) {
            player.closeInventory();

            if (plugin.getStoneMachine().repairStoneMachine(machineState)) {
                new Message(plugin.getLanguage("stone-machine-repaired")).send(player);
            } else {
                new Message(plugin.getLanguage("stone-machine-repaired-deny")).send(player);
            }
        }

        //Mining statistics
        else if (clickedPoint.getSlotNumber() == 25) {
            player.closeInventory();

            player.performCommand("dropstat");
        }

        //Stone machine's README / usage instructions
        else if (clickedPoint.getSlotNumber() == 26) {
            player.closeInventory();

            player.performCommand("drophelp");
        }

    }

}
