/*
 * Copyright Go2Play.pl (c) 2020.
 * Program made for Go2Play Skyblock server. It's not allowed to re-distribute the code.
 * Author: FlrQue
 */

package win.flrque.g2p.stoneage.gui.window;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import win.flrque.g2p.stoneage.database.playerdata.PlayerConfig;
import win.flrque.g2p.stoneage.database.playerdata.PlayerStats;
import win.flrque.g2p.stoneage.drop.DropCalculator;
import win.flrque.g2p.stoneage.drop.DropEntry;
import win.flrque.g2p.stoneage.drop.DropMultiplier;
import win.flrque.g2p.stoneage.gui.InventoryPoint;
import win.flrque.g2p.stoneage.gui.Window;
import win.flrque.g2p.stoneage.util.Message;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class DropInfoWindow extends Window {

    private final Player windowContentOwner;
    private final PlayerConfig personalDropConfig;

    final DecimalFormat df = new DecimalFormat("0.00");

    //TODO: Support for pagination
    public DropInfoWindow(Player owner) {
        super(ChatColor.translateAlternateColorCodes('&', "&7&lSTONIARKA &8&l> &5&lDROP INFO"));

        windowContentOwner = owner;
        personalDropConfig = plugin.getPlayersData().getPersonalDropConfig(windowContentOwner.getUniqueId());
    }

    @Override
    public void updateInventoryContent() {

        final DropCalculator calculator = plugin.getDropCalculator();
        final PlayerStats stats = plugin.getPlayersData().getPlayerStoneMachineStats(windowContentOwner.getUniqueId());

        for (int i = 0; i <= calculator.getDropEntries().size(); i++) {
            if (i >= inventory.getSize()) {
                break;
            }

            final DropEntry drop;

            if (i == calculator.getDropEntries().size()) drop = calculator.getPrimitiveDropEntry();
            else drop = calculator.getDropEntries().get(i);

            final ItemStack icon = createIconItem(drop, stats);

            inventory.setItem(i, icon);
        }
    }

    @NotNull
    private ItemStack createIconItem(@NotNull DropEntry drop, @NotNull PlayerStats stats) {
        //Preparing data to be placed on the item
        final DropCalculator calculator = plugin.getDropCalculator();
        final float currentDropMultiplier = calculator.getDropMultiplier().getCurrentDropMultiplier();
        final float dropChance = getChancePercentage(drop);

        final ItemStack icon = drop.getDropEntryIcon();
        icon.setAmount(1);

        final ItemMeta meta = icon.getItemMeta();
        meta.setDisplayName(ChatColor.GREEN + Message.simplePrepare(drop.getCustomName()) + ChatColor.GOLD + " (" + df.format(dropChance) + "%)");

        final List<String> lore = new ArrayList<>();
        lore.add(Message.color("  &8+" + drop.getMinerExp() + "xp"));
        final String dropEntryStatus = personalDropConfig.isDropping(drop) ? "&2Wlaczony" : "&cWylaczony";
        lore.add(Message.color("&7Status: " + dropEntryStatus));
        lore.add(Message.color("&7(Kliknij aby zmienic)"));
        lore.add(Message.EMPTY);
        lore.add(Message.color("&cWykopano juz: " + stats.getStatistic(drop.getEntryName())));

        if (calculator.getDropMultiplier().isActive()) {
            lore.add(" "); // spacer

            final float realDropChance = getRealChancePercentage(drop);
            lore.add(Message.color("&7Rzeczywisty drop: " + df.format(realDropChance) + "%"));
        }

        meta.setLore(lore);

        icon.setItemMeta(meta);

        return icon;
    }

    private float getChancePercentage(DropEntry drop) {
        return (100 * drop.getChanceWeight() / plugin.getDropCalculator().getTotalWeight());
    }

    private float getRealChancePercentage(DropEntry drop) {
        final DropCalculator calculator = plugin.getDropCalculator();
        final DropMultiplier dropMultiplier = calculator.getDropMultiplier();

        final float multiplier = (drop.isMultipliable() ? dropMultiplier.getCurrentDropMultiplier() : 1.0f);

        return (((drop.getChanceWeight() * multiplier) / plugin.getDropCalculator().getTotalWeight()) * 100);
    }

    @Override
    public boolean open(Player player) {
        if (!super.open(player)) {
            new Message("&cNie udało się otworzyć okna dropu!").send(player);
            return false;
        }

        player.openInventory(inventory);

        return true;
    }

    @Override
    public void onClick(ClickType clickType, Player player, InventoryPoint clickedPoint) {
        final int clickedSlot = clickedPoint.getSlotNumber();

        final DropCalculator calculator = plugin.getDropCalculator();
        final DropEntry dropEntry;

        if (clickedSlot < calculator.getDropEntries().size())
            dropEntry = calculator.getDropEntries().get(clickedSlot);
        else if (clickedSlot == calculator.getDropEntries().size())
            dropEntry = calculator.getPrimitiveDropEntry();
        else {
            // Clicked on empty slot perhaps
            return;
        }

        //Closing to reduce inventory update lag
        player.closeInventory();

        boolean isDropping = plugin.getPlayersData().getPersonalDropConfig(player.getUniqueId()).switchDropEntry(dropEntry);

        final Message infoMessage = new Message("&7Ustawiono drop &c$_1 &7na $_2&7.");
        infoMessage.setVariable(1, dropEntry.getCustomName());
        infoMessage.setVariable(2, (isDropping ? "&2wlaczony" : "&cwylaczony"));
        infoMessage.send(player);
    }

}
