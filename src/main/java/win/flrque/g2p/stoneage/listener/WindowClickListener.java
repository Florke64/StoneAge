/*
 * Copyright Go2Play.pl (c) 2020.
 * Program made for Go2Play Skyblock server. It's not allowed to re-distribute the code.
 * Author: FlrQue
 */

package win.flrque.g2p.stoneage.listener;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import win.flrque.g2p.stoneage.StoneAge;
import win.flrque.g2p.stoneage.gui.InventoryPoint;
import win.flrque.g2p.stoneage.gui.Window;
import win.flrque.g2p.stoneage.gui.WindowManager;

public class WindowClickListener implements Listener {

    private final StoneAge plugin;

    public WindowClickListener() {
        plugin = StoneAge.getPlugin(StoneAge.class);
    }

    @EventHandler
    public void onInventoryClick(@NotNull InventoryClickEvent event) {
        // Only players can use GUI.
        if (!(event.getWhoClicked() instanceof Player))
            return;

        final Player player = (Player) event.getWhoClicked();

        final Inventory clickedInventory = event.getWhoClicked().getOpenInventory().getTopInventory();

        // Checking if clicked Inventory matches one of the supported Windows.
        final WindowManager manager = plugin.getWindowManager();
        Window window = manager.getWindow(player);
        if (window == null) return;

        if (!window.getBukkitInventory().equals(clickedInventory)) return;

        // Cancelling item moving.
        event.setCurrentItem(event.getCurrentItem());
        event.setCancelled(true);

        ClickType clickType = event.getClick();
        InventoryPoint clickedPoint = new InventoryPoint(event.getInventory().getType(), event.getRawSlot());


        ItemStack current = event.getCurrentItem();
        if (current != null && !current.getType().equals(Material.AIR))
            window.onClick(clickType, player, clickedPoint);
    }

}
