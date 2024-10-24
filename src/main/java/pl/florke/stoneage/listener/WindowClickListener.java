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

package pl.florke.stoneage.listener;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import pl.florke.stoneage.StoneAge;
import pl.florke.stoneage.gui.InventoryPoint;
import pl.florke.stoneage.gui.Window;
import pl.florke.stoneage.gui.WindowManager;

public class WindowClickListener implements Listener {

    private final StoneAge plugin;

    public WindowClickListener() {
        plugin = StoneAge.getPlugin(StoneAge.class);
    }

    @EventHandler
    public void onInventoryClick(@NotNull InventoryClickEvent event) {
        // Only players can use GUI.
        if (!(event.getWhoClicked() instanceof Player player))
            return;

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
