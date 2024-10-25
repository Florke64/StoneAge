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
import pl.florke.stoneage.StoneAge;
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
import java.util.Arrays;
import java.util.List;

public class DropInfoWindow extends Window {

    final DecimalFormat df = new DecimalFormat("0.00");
    private final Player windowContentOwner;
    private final PlayerConfig personalDropConfig;

    //TODO: Support for pagination
    public DropInfoWindow(Player owner) {
        super(Message.color(StoneAge.getPlugin(StoneAge.class)
                .getLanguage("stone-drop-info-title")));

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
        final float dropChance = getChancePercentage(drop);

        final ItemStack icon = drop.getDropEntryIcon();
        icon.setAmount(1);

        final ItemMeta meta = icon.getItemMeta();

        final boolean playerHasNeededLevelForDrop = stats.getMinerLvl() >= drop.getNeededMinerLevel();

        final String dropEntryNameDetails;
        if (playerHasNeededLevelForDrop) {
            dropEntryNameDetails = Message.color("&6 (" + df.format(dropChance) + "%)");
        } else {
            dropEntryNameDetails = "";
        }

        meta.displayName(new Message(
                "&6" + Message.constNamePrettify(drop.getCustomName()) + dropEntryNameDetails
        ).asComponents()[0]);

        final Message lore = new Message();
        if (!playerHasNeededLevelForDrop) {
            lore.addLines(plugin.getLanguage("stone-drop-info-lvl-required"))
                    .placeholder(1, String.valueOf(drop.getNeededMinerLevel()));
        } else {
            lore.addLines("  &8+" + drop.getMinerExp() + "xp");

            final String dropEntryStatusRaw = personalDropConfig.isDropping(drop) ?
                    plugin.getLanguage("system-enabled") : plugin.getLanguage("system-disabled");
            lore.addLines(plugin.getLanguage("stone-drop-info-entry-status"))
                    .placeholder(2, dropEntryStatusRaw);
            lore.addLines(plugin.getLanguage("stone-drop-info-click-to-switch"));

            lore.addLines(" ");
            // command-feedback-drop-print-summary is used multiple times and its placeholder handler is $_4
            lore.addLines(plugin.getLanguage("command-feedback-drop-print-summary"))
                    .placeholder(4, String.valueOf(stats.getStatistic(drop.getEntryName())));
        }

        if (calculator.getDropMultiplier().isActive()) {
            lore.addLines(" "); // spacer

            final float realDropChance = getRealChancePercentage(drop);
            lore.addLines(plugin.getLanguage("stone-machine-drop-chance"))
                    .placeholder(3, String.valueOf(realDropChance));
        }

        meta.lore(Arrays.asList(lore.asComponents()));

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
    public boolean open(@NotNull Player player) {
        if (!super.open(player)) {
            new Message(plugin.getLanguage("stone-drop-gui-error")).send(player);
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

        final Message infoMessage = new Message(plugin.getLanguage("stone-machine-drop-switch"));
        infoMessage.placeholder(1, dropEntry.getCustomName());
        infoMessage.placeholder(2, (isDropping ?
                plugin.getLanguage("system-enabled") :
                plugin.getLanguage("system-disabled")));
        infoMessage.send(player);
    }

}
