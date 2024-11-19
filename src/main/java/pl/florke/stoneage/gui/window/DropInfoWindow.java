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

import net.kyori.adventure.text.TextComponent;
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
import java.util.List;

public class DropInfoWindow extends Window {

    final DecimalFormat df = new DecimalFormat("0.00");

    private final Player windowContentOwner;
    private final PlayerConfig personalDropConfig;

    //TODO: Support for pagination
    public DropInfoWindow(Player owner) {
        super (
            new Message(StoneAge.getPlugin(StoneAge.class)
                .getLanguage("stone-drop-info-title")).asComponents().getFirst()
        );

        windowContentOwner = owner;
        personalDropConfig = plugin.getPlayersData().getPersonalDropConfig(windowContentOwner.getUniqueId());
    }

    @Override
    public void updateInventoryContent() {
        final PlayerStats stats = plugin.getPlayersData().getPlayerStoneMachineStats(windowContentOwner.getUniqueId());

        final DropCalculator calculator = plugin.getDropCalculator();
        int i = drawDropEntries(calculator.getCustomDropEntries(), stats, 0);

        final List<DropEntry> dropEntries = calculator.getDropResourcesEntries().values().stream().toList();
        drawDropEntries(dropEntries, stats, i);
    }

    private int drawDropEntries(@NotNull List<DropEntry> dropEntries, @NotNull PlayerStats stats, int startIndex) {
        int i = startIndex;
        for (DropEntry drop : dropEntries) {
            if (i >= inventory.getSize())
                break;

            final ItemStack icon = createIconItem(drop, stats);

            inventory.setItem(i, icon);

            i++;
        }

        return i;
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
            dropEntryNameDetails = Message.colors("&6 (" + df.format(dropChance) + "%)");
        } else {
            dropEntryNameDetails = "";
        }

        if (drop.getCustomName() == null || drop.getCustomName().isEmpty()) {
            String iconRawName = ((TextComponent) drop.getDropEntryIcon().displayName()).content();
            final Message dropTitle = new Message("&6" + Message.constNamePrettify(iconRawName) + dropEntryNameDetails);
            meta.displayName(dropTitle.asComponents().getFirst());
        }

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
                    .placeholder(4, String.valueOf(stats.getStatistic(drop.getKey())));
        }

        if (calculator.getDropMultiplier().isActive()) {
            lore.addLines(" "); // spacer

            final float realDropChance = getRealChancePercentage(drop);
            lore.addLines(plugin.getLanguage("stone-machine-drop-chance"))
                    .placeholder(3, String.valueOf(realDropChance));
        }

        meta.lore(lore.asComponents());
        icon.setItemMeta(meta);

        return icon;
    }

    private float getChancePercentage(DropEntry drop) {
        return (100 * drop.getChanceWeight() / plugin.getDropCalculator().getTotalDropsWeight());
    }

    private float getRealChancePercentage(DropEntry drop) {
        final DropCalculator calculator = plugin.getDropCalculator();
        final DropMultiplier dropMultiplier = calculator.getDropMultiplier();

        final float multiplier = (drop.isMultipliable() ? dropMultiplier.getCurrentDropMultiplier() : 1.0f);

        return (((drop.getChanceWeight() * multiplier) / (
                drop.getEntryType().equals(DropEntry.EntryType.RESOURCE_DROP)?
                        plugin.getDropCalculator().getTotalResourcesWeight() : plugin.getDropCalculator().getTotalDropsWeight()
        )) * 100);
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

        final int customDropsAmount = calculator.getCustomDropEntries().size();
        final int resourcesAmount = calculator.getDropResourcesEntries().size();

        if (clickedSlot < customDropsAmount)
            dropEntry = calculator.getCustomDropEntries().get(clickedSlot);

        else if (clickedSlot >= calculator.getCustomDropEntries().size())
            dropEntry = List.of(calculator.getDropResourcesEntries().sequencedValues())
                    .get(clickedSlot - customDropsAmount).getFirst();

        else // Clicked on an empty slot, perhaps
            return;

        //Closing to reduce inventory update lag
        player.closeInventory();

        boolean isDropping = plugin.getPlayersData().getPersonalDropConfig(player.getUniqueId()).switchDropEntry(dropEntry);

        new Message(plugin.getLanguage("stone-machine-drop-switch"))
            .placeholder(1, dropEntry.getCustomName())
            .placeholder(2, (isDropping ?
                plugin.getLanguage("system-enabled") :
                plugin.getLanguage("system-disabled")
            )).send(player);
    }

}
