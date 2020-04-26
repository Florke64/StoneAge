package win.flrque.g2p.stoneage.gui;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ItemButtonFactory {



    public enum ItemButtonType {
        DROP_FILTER(new ItemStack(Material.HOPPER), "&3Filtruj Drop");

        final ItemStack itemIcon;

        ItemButtonType(final ItemStack icon, final String label) {
            final ItemMeta meta = icon.getItemMeta();
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', label));
            icon.setItemMeta(meta);

            this.itemIcon = icon;
        }

        public ItemStack toItemStack() {
            return itemIcon;
        }
    }
}
