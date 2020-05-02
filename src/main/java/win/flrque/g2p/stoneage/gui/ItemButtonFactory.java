/*
 * Copyright Go2Play.pl (c) 2020.
 * Program made for Go2Play Skyblock server. It's not allowed to re-distribute the code.
 * Author: FlrQue
 */

package win.flrque.g2p.stoneage.gui;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import win.flrque.g2p.stoneage.StoneAge;

import java.util.ArrayList;
import java.util.List;

public class ItemButtonFactory {

    private final StoneAge plugin;

    private final Player player;

    public ItemButtonFactory(Player player) {
        plugin = StoneAge.getPlugin(StoneAge.class);

        this.player = player;
    }


    public ItemStack getButton(ItemButtonType buttonType) {
        return null;
    }

    private enum ItemButtonType {
        DROP_FILTER(new ItemStack(Material.HOPPER), "&3Filtruj Drop"),
        DROP_INFO(new ItemStack(Material.DIAMOND_ORE), "&2Sprawdz Drop"),
        DROP_MULTIPLIER(new ItemStack(Material.END_CRYSTAL), "&5MNOŻNIK"),
        MACHINE_REPAIR(new ItemStack(Material.ANVIL), "&4Napraw Generator", "&4Uwaga! &eMozesz naprawiac stoniarke", "&eco kilka sekund!"),
        MACHINE_UPGRADE(new ItemStack(Material.EMERALD), "&6Ulepsz stoniarke!", "&4Uwaga! &eZakupione ulepszenia znikaja", "&epo zniszczeniu maszyny!"),
        MACHINE_INFO(new ItemStack(Material.REDSTONE_TORCH_ON), "&5Informacje", "&7Kliknij i przeczytaj", "&7informacje dt. generatowow kamienia,"),
        STONE_STATISTICS(new ItemStack(Material.KNOWLEDGE_BOOK), "&3Statystyki", "&7Otworz okno ze swoimi statystykami.");

        final ItemStack itemIcon;

        ItemButtonType(@NotNull final ItemStack icon, final String label, @NotNull final String ...rawLore) {
            final ItemMeta meta = icon.getItemMeta();
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', label));

            final List<String> lore = new ArrayList<>();
            for(String line : rawLore) {
                lore.add(ChatColor.translateAlternateColorCodes('&', line));
            }

            if(!lore.isEmpty()) {
                meta.setLore(lore);
            }

            icon.setItemMeta(meta);

            this.itemIcon = icon;
        }

        public ItemStack toItemStack() {
            return itemIcon;
        }
    }
}
