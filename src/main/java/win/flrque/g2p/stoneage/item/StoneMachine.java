package win.flrque.g2p.stoneage.item;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class StoneMachine {

    public static ItemStack getStoneMachineItem() {
        final ItemStack item = new ItemStack(Material.DISPENSER);
        final ItemMeta meta = item.getItemMeta();

        //TODO: Add appropriate configuration sections
        meta.setDisplayName(ChatColor.GOLD + "Stoniarka");

        final List<String> lore = new ArrayList<>();
        meta.setLore(lore);

        item.setItemMeta(meta);

        return item;
    }

}
