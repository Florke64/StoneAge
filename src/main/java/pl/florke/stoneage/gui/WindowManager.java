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

import org.bukkit.block.Dispenser;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.PlayerInventory;

import java.util.HashMap;
import java.util.Map;

public class WindowManager {

    private final Map<Player, Window> cachedWindows = new HashMap<>();
    private final Map<Dispenser, Window> cachedMachines = new HashMap<>();

    public Window getWindow(Player player) {
        return cachedWindows.get(player);
    }

    public Window getWindow(Dispenser machineBlockState) {
        return cachedMachines.get(machineBlockState);
    }

    public void cacheWindow(Player player, Window window) {
        cachedWindows.put(player, window);
    }

    public void cacheMachine(Dispenser blockState, Window window) {
        cachedMachines.put(blockState, window);
    }

    public void closeAllWindows() {
        for (Player player : cachedWindows.keySet()) {
            if (player != null && player.isOnline()) {
                if (player.getOpenInventory() instanceof PlayerInventory)
                    continue;

                final Inventory openInventory = player.getOpenInventory().getTopInventory();
                if (getWindow(player).getBukkitInventory().equals(openInventory)) {
                    player.closeInventory();
                }
            }
        }
    }
}
