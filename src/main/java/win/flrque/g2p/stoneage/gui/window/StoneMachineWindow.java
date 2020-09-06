/*
 * Copyright Go2Play.pl (c) 2020.
 * Program made for Go2Play Skyblock server. It's not allowed to re-distribute the code.
 * Author: FlrQue
 */

package win.flrque.g2p.stoneage.gui.window;

import org.bukkit.ChatColor;
import org.bukkit.block.Dispenser;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryType;
import org.jetbrains.annotations.NotNull;
import win.flrque.g2p.stoneage.gui.InventoryPoint;
import win.flrque.g2p.stoneage.gui.ItemButtonFactory;
import win.flrque.g2p.stoneage.gui.ItemButtonFactory.ItemButtonType;
import win.flrque.g2p.stoneage.gui.Window;
import win.flrque.g2p.stoneage.machine.ItemAutoSmelter;
import win.flrque.g2p.stoneage.util.Message;

public class StoneMachineWindow extends Window {

    private final Player windowOwner;

    private final Dispenser stoneMachine;

    public StoneMachineWindow(Player owner, Dispenser stoneMachine) {
        super(ChatColor.translateAlternateColorCodes('&', "&7&lSTONIARKA &8&l> &5&lOPCJE"));

        windowOwner = owner;
        this.stoneMachine = stoneMachine;
    }

    public Dispenser getStoneMachine() {
        return stoneMachine;
    }

    public Player getWindowOwner() {
        return windowOwner;
    }

    @Override
    public boolean open(Player player) {
        if (!super.open(player)) {
            new Message("&cNie udało się otworzyć okna stoniarki!").send(player);
            return false;
        }

        plugin.getWindowManager().cacheMachine(stoneMachine, this);
        player.openInventory(inventory);

        return true;
    }

    @Override
    public void updateInventoryContent() {
        final ItemButtonFactory buttonFactory = new ItemButtonFactory(windowOwner);

        inventory.setItem(new InventoryPoint(InventoryType.CHEST, 0, 0).getSlotNumber(), buttonFactory.getButton(ItemButtonType.DROP_FILTER));
//        inventory.setItem(new InventoryPoint(InventoryType.CHEST, 0,1).getSlotNumber(), buttonFactory.getButton(ItemButtonType.DROP_INFO));

        inventory.setItem(new InventoryPoint(InventoryType.CHEST, 4, 0).getSlotNumber(), buttonFactory.getButton(ItemButtonType.DROP_MULTIPLIER));
        inventory.setItem(new InventoryPoint(InventoryType.CHEST, 8, 0).getSlotNumber(), buttonFactory.getButton(ItemButtonType.AUTO_SMELTING_STATUS));
        inventory.setItem(new InventoryPoint(InventoryType.CHEST, 4, 2).getSlotNumber(), buttonFactory.getButton(ItemButtonType.MACHINE_REPAIR));

//        inventory.setItem(new InventoryPoint(InventoryType.CHEST, 8,0).getSlotNumber(), buttonFactory.getButton(ItemButtonType.MACHINE_UPGRADE));

        inventory.setItem(new InventoryPoint(InventoryType.CHEST, 7, 2).getSlotNumber(), buttonFactory.getButton(ItemButtonType.STONE_STATISTICS));
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
            final int usesLeft = autoSmelter.getAutoSmeltingUsesLeft(stoneMachine);

            final Message msg = new Message();
            if (usesLeft > 0) {
                msg.addLines("&7Pozostalo &6$_1 &7uzyc automatycznego przepalania.");
                msg.setVariable(1, Integer.toString(usesLeft));
            } else {
                msg.addLines("&6Wprowadz wegiel hopperem, a stoniarka bedzie przepalac automatycznie!");
            }

            msg.send(player);
        }

        //Repair Stone Machine
        else if (clickedPoint.getSlotNumber() == 22) {
            player.closeInventory();

            if (plugin.getStoneMachine().repairStoneMachine(stoneMachine)) {
                new Message("&7Naprawiono stoniarke!").send(player);
            } else {
                new Message("&cNie udalo sie naprawic tej stoniarki, sprobuj ponownie pozniej...").send(player);
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
