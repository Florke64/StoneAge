package win.flrque.g2p.stoneage.item;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class StoneMachine {

    public static final Material STONE_MACHINE_MATERIAL = Material.DISPENSER;

    private static final ItemStack stoneMachineParent = getStoneMachineItem(STONE_MACHINE_MATERIAL);

    public static ItemStack getStoneMachineItem() {
        return stoneMachineParent.clone();
    }

    private static ItemStack getStoneMachineItem(Material material) {
        final ItemStack item = new ItemStack(material);
        final ItemMeta meta = item.getItemMeta();

        //TODO: Add appropriate configuration sections
        meta.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + "Stoniarka");

        final List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Postaw stoniarke w kierunku,");
        lore.add(ChatColor.GRAY + "gdzie ma generowac stone!");
        lore.add(" ");
        lore.add(ChatColor.DARK_RED + "Uwaga! " + ChatColor.YELLOW + "Stoniarki mozna niszczyc");
        lore.add(ChatColor.YELLOW + "tylko zlotym kilofem.");
        meta.setLore(lore);

        item.setItemMeta(meta);

        return item;
    }

    public static ItemStack getExampleStoneMachine() {
        return stoneMachineParent;
    }

}
