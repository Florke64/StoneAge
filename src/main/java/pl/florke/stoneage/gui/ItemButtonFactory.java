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

package pl.florke.stoneage.gui;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import pl.florke.stoneage.StoneAge;
import pl.florke.stoneage.drop.DropMultiplier;

import java.util.ArrayList;
import java.util.List;

public class ItemButtonFactory {

    private final StoneAge plugin;

    private final Player player;

    public ItemButtonFactory(Player player) {
        plugin = StoneAge.getPlugin(StoneAge.class);

        this.player = player;
    }

    public ItemStack getButton(@NotNull ItemButtonType buttonType) {
        switch (buttonType) {
            case DROP_MULTIPLIER:
                return getUpdatedDropMultiplier();
            case DROP_INFO:
            case DROP_FILTER:
            case MACHINE_INFO:
            case MACHINE_REPAIR:
            case MACHINE_UPGRADE:
            case STONE_STATISTICS:
            default:
                return buttonType.getItemIcon();
        }
    }

    @NotNull
    private ItemStack getUpdatedDropMultiplier() {
        final ItemStack item = ItemButtonType.DROP_MULTIPLIER.getItemIcon();
        final ItemMeta meta = item.getItemMeta();

        final DropMultiplier multiplier = plugin.getDropCalculator().getDropMultiplier();
        final List<String> lore = new ArrayList<>();

        if (multiplier.isActive()) {
            lore.add(ChatColor.translateAlternateColorCodes('&', "&7Aktywny mnoznik &3x" + multiplier.getCurrentDropMultiplier() + "."));

            final long timeoutInMinutes = ((multiplier.getMultiplierTimeout() - System.currentTimeMillis()) / 1000) / 60;
            lore.add(ChatColor.translateAlternateColorCodes('&', "&7Wygasnie za &4" + timeoutInMinutes + " &7minut."));
        } else {
            lore.add(ChatColor.translateAlternateColorCodes('&', "&7Mnoznik dropu nie jest aktywny..."));
        }

        meta.setLore(lore);
        item.setItemMeta(meta);

        return item;
    }

    public enum ItemButtonType {
        DROP_FILTER(new ItemStack(Material.HOPPER), "&3&lFiltruj Drop"),
        DROP_INFO(new ItemStack(Material.DIAMOND_ORE), "&2&lSprawdz Drop"),
        DROP_MULTIPLIER(new ItemStack(Material.END_CRYSTAL), "&5&lMnoznik"),
        MACHINE_REPAIR(new ItemStack(Material.ANVIL), "&4&lNapraw Generator", "&4Uwaga! &eMozesz naprawiac stoniarke", "&eco kilka sekund!"),
        MACHINE_UPGRADE(new ItemStack(Material.EMERALD), "&6&lUlepsz stoniarke!", "&4Uwaga! &eZakupione ulepszenia znikaja", "&epo zniszczeniu maszyny!"),
        AUTO_SMELTING_STATUS(new ItemStack(Material.BLAST_FURNACE), "&6&lAutomatyczne przepalanie", "&7Kliknij aby przeczytac detale."),
        MACHINE_INFO(new ItemStack(Material.REDSTONE_TORCH), "&5&lInformacje", "&7Kliknij i przeczytaj", "&7informacje dt. generatowow kamienia."),
        STONE_STATISTICS(new ItemStack(Material.KNOWLEDGE_BOOK), "&3&lStatystyki", "&7Zobacz swoje statystyki.");

        final ItemStack itemIcon;

        ItemButtonType(@NotNull final ItemStack icon, final String label, @NotNull final String... rawLore) {
            final ItemMeta meta = icon.getItemMeta();
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', label));

            final List<String> lore = new ArrayList<>();
            for (String line : rawLore) {
                lore.add(ChatColor.translateAlternateColorCodes('&', line));
            }

            if (!lore.isEmpty()) {
                meta.setLore(lore);
            }

            icon.setItemMeta(meta);

            this.itemIcon = icon;
        }

        public ItemStack getItemIcon() {
            return itemIcon.clone();
        }
    }
}
