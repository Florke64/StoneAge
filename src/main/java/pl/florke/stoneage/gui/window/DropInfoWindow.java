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

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import pl.florke.stoneage.database.playerdata.PlayerConfig;
import pl.florke.stoneage.database.playerdata.PlayerStats;
import pl.florke.stoneage.drop.DropCalculator;
import pl.florke.stoneage.drop.DropEntry;
import pl.florke.stoneage.drop.DropMultiplier;
import pl.florke.stoneage.gui.InventoryPoint;
import pl.florke.stoneage.gui.Window;
import pl.florke.stoneage.util.Message;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class DropInfoWindow extends Window {

    final DecimalFormat df = new DecimalFormat("0.00");
    private final Player windowContentOwner;
    private final PlayerConfig personalDropConfig;

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

        final boolean playerHasNeededLevelForDrop = stats.getMinerLvl() >= drop.getNeededMinerLevel();

        final String dropEntryNameDetails;
        if (playerHasNeededLevelForDrop) {
            dropEntryNameDetails = ChatColor.GOLD + " (" + df.format(dropChance) + "%)";
        } else {
            dropEntryNameDetails = "";
        }

        meta.setDisplayName(ChatColor.GREEN + Message.simplePrepare(drop.getCustomName()) + dropEntryNameDetails);

        final List<String> lore = new ArrayList<>();
        if (!playerHasNeededLevelForDrop) {
            lore.add(ChatColor.RED + "Wymagany lvl: " + drop.getNeededMinerLevel());
        } else {
            lore.add(Message.color("  &8+" + drop.getMinerExp() + "xp"));

            final String dropEntryStatus = personalDropConfig.isDropping(drop) ? "&2Wlaczony" : "&cWylaczony";
            lore.add(Message.color("&7Status: " + dropEntryStatus));
            lore.add(Message.color("&7(Kliknij aby zmienic)"));
            lore.add(Message.EMPTY);
            lore.add(Message.color("&cWykopano juz: " + stats.getStatistic(drop.getEntryName())));
        }

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
