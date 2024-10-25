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

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import pl.florke.stoneage.StoneAge;
import pl.florke.stoneage.drop.DropMultiplier;
import pl.florke.stoneage.util.Language;
import pl.florke.stoneage.util.Message;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ItemButtonFactory {

    private final StoneAge plugin;

    public ItemButtonFactory() {
        plugin = StoneAge.getPlugin(StoneAge.class);
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
            case DROP_STATISTICS:
            case SMELTING_STATUS:
            default:
                return buttonType.getItemIcon();
        }
    }

    @NotNull
    private ItemStack getUpdatedDropMultiplier() {
        final ItemStack item = ItemButtonType.DROP_MULTIPLIER.getItemIcon();
        final ItemMeta meta = item.getItemMeta();

        final DropMultiplier multiplier = plugin.getDropCalculator().getDropMultiplier();
        final Language language = plugin.getLanguage();
        final Message lore = new Message();

        if (multiplier.isActive())
            lore.addLines(language.getText("command-feedback-multiplier-active"));
        else
            lore.addLines(language.getText("command-feedback-multiplier-inactive"));

        lore.placeholder(1, multiplier.getCallerName());
        lore.placeholder(2, String.valueOf(multiplier.getCurrentDropMultiplier()));
        lore.placeholder(3, String.valueOf(multiplier.getMinutesLeft()));

        meta.lore(Arrays.asList(lore.asComponents()));
        item.setItemMeta(meta);

        return item;
    }

    public enum ItemButtonType {
        DROP_INFO(new ItemStack(Material.DIAMOND_ORE)),
        DROP_FILTER(new ItemStack(Material.HOPPER)),
        DROP_STATISTICS(new ItemStack(Material.KNOWLEDGE_BOOK)),
        DROP_MULTIPLIER(new ItemStack(Material.END_CRYSTAL)),
        MACHINE_INFO(new ItemStack(Material.REDSTONE_TORCH)),
        MACHINE_REPAIR(new ItemStack(Material.ANVIL)),
        MACHINE_UPGRADE(new ItemStack(Material.EMERALD)),
        SMELTING_STATUS(new ItemStack(Material.BLAST_FURNACE));

        final ItemStack itemIcon;

        ItemButtonType(@NotNull final ItemStack icon) {
            final Language language = StoneAge.getPlugin(StoneAge.class).getLanguage();
            final List<Component> lore = new ArrayList<>();

            final ItemMeta meta = icon.getItemMeta();

            final Component[] itemDescription = new Message(language.getText(
                "stone-drop-gui-icon-" + this.configLineSuffix())
            ).asComponents();

            // Set first line as display name
            // and other lines as lore for item
            for (int i = 0; i < itemDescription.length; i++)
                if (i == 0)
                    meta.displayName(itemDescription[i]);
                else
                    lore.add(itemDescription[i]);

            if (!lore.isEmpty())
                meta.lore(lore);

            icon.setItemMeta(meta);

            this.itemIcon = icon;
        }

        @NotNull
        public ItemStack getItemIcon() {
            return itemIcon.clone();
        }

        @NotNull
        public String configLineSuffix() {
            return name().replace('_', '-').toLowerCase();
        }
    }
}
