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
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import pl.florke.stoneage.StoneAge;

import java.util.ArrayList;
import java.util.List;

public abstract class Window {

    protected final StoneAge plugin;
    protected final String windowTitle;
    private final List<Player> users;
    protected final Inventory inventory;

    public Window(final String windowName) {
        plugin = StoneAge.getPlugin(StoneAge.class);
        users = new ArrayList<>();

        windowTitle = windowName;
        inventory = Bukkit.createInventory(null, 3*9, Component.text(windowName));
    }

    public boolean open(@NotNull Player player) {
        player.closeInventory();

        if (player.isSleeping()) {
            return false;
        }

        plugin.getWindowManager().cacheWindow(player, this);
        users.add(player);

        new BukkitRunnable() {
            @Override
            public void run() {
                updateInventoryContent();
            }
        }.runTaskAsynchronously(plugin);

        return true;
    }

    public List<Player> getUsers() {
        return users;
    }

    public abstract void updateInventoryContent();

    public Inventory getBukkitInventory() {
        return inventory;
    }

    public abstract void onClick(@SuppressWarnings("unused") ClickType clickType, Player player, InventoryPoint clickedPoint);

}
