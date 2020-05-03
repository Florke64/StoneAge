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
import win.flrque.g2p.stoneage.drop.DropCalculator;
import win.flrque.g2p.stoneage.drop.DropEntry;
import win.flrque.g2p.stoneage.drop.DropMultiplier;
import win.flrque.g2p.stoneage.drop.PersonalDropConfig;
import win.flrque.g2p.stoneage.gui.InventoryPoint;
import win.flrque.g2p.stoneage.gui.Window;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class DropInfoWindow extends Window  {

    private final Player windowContentOwner;
    private final PersonalDropConfig personalDropConfig;

    final DecimalFormat df = new DecimalFormat();

    //TODO: Support for pagination
    public DropInfoWindow(Player owner) {
        super(ChatColor.translateAlternateColorCodes('&', "&7&lSTONIARKA &8&l> &5&lDROP INFO"));

        windowContentOwner = owner;
        personalDropConfig = plugin.getDropCalculator().getPersonalDropConfig(windowContentOwner);

        df.setMaximumFractionDigits(2);
        df.setMinimumFractionDigits(2);
    }

    @Override
    public void updateInventoryContent() {

        final DropCalculator calculator = plugin.getDropCalculator();

        for(int i=0; i<=calculator.getDropEntries().size(); i++) {
            final DropEntry drop;

            if(i == calculator.getDropEntries().size()) drop = calculator.getPrimitiveDropEntry();
            else drop = calculator.getDropEntries().get(i);

            final ItemStack icon = createIconItem(drop);

            inventory.setItem(i, icon);
        }
    }

    private ItemStack createIconItem(DropEntry drop) {
        //Preparing data to be placed on the item
        final DropCalculator calculator = plugin.getDropCalculator();
        final float currentDropMultiplier = calculator.getDropMultiplier().getCurrentDropMultiplier();
        final float dropChance = getChancePercentage(drop);

        final ItemStack icon = drop.getDropEntryIcon();
        icon.setAmount(1);

        final ItemMeta meta = icon.getItemMeta();
        meta.setDisplayName(ChatColor.GREEN + icon.getType().toString() + ChatColor.GOLD +" ("+ df.format(dropChance) +"%)");

        final List<String> lore = new ArrayList<>();
        final String dropEntryStatus = personalDropConfig.isDropping(drop)? "&2Wlaczony" : "&cWylaczony";
        lore.add(ChatColor.translateAlternateColorCodes('&', "&7Status: " + dropEntryStatus));
        lore.add(ChatColor.translateAlternateColorCodes('&', "&7(Kliknij aby zmienic)"));

        if(currentDropMultiplier != calculator.getDropMultiplier().getDefaultDropMultiplier()) {
            lore.add(" "); // spacer

            final float realDropChance = getRealChancePercentage(drop);
            lore.add(ChatColor.translateAlternateColorCodes('&', "&7Rzeczywisty drop: " + df.format(realDropChance) + "%"));
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

        final float multiplier = (drop.isMultipliable()? dropMultiplier.getCurrentDropMultiplier() : 1.0f);

        return (((drop.getChanceWeight() * multiplier) / plugin.getDropCalculator().getTotalWeight()) * 100);
    }

    @Override
    public boolean open(Player player) {
        if(!super.open(player)) {
            player.sendMessage("Nie udało się otworzyć okna dropu!");
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

        if(clickedSlot < calculator.getDropEntries().size())
            dropEntry = calculator.getDropEntries().get(clickedSlot);
        else if(clickedSlot == calculator.getDropEntries().size())
            dropEntry = calculator.getPrimitiveDropEntry();
        else {

            return; //Clicked on empty slot perhaps
        }

        //Closing to reduce inventory update lag
        player.closeInventory();

        boolean isDropping = calculator.getPersonalDropConfig(player).switchDropEntry(dropEntry);
        player.sendMessage("Ustawiono drop " + dropEntry.getDropEntryIcon().getType() + " na " + (isDropping? "wlaczony" : "wylaczony"));
    }

}
