/*
 * Copyright Go2Play.pl (c) 2020.
 * Program made for Go2Play Skyblock server. It's not allowed to re-distribute the code.
 * Author: FlrQue
 */

package win.flrque.g2p.stoneage.gui;

import org.bukkit.block.Dispenser;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

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
        for(Player player : cachedWindows.keySet()) {
            if(player != null && player.isOnline()) {
                if(player.getOpenInventory() == null)
                    continue;

                final Inventory openInventory = player.getOpenInventory().getTopInventory();
                if(getWindow(player).getBukkitInventory().equals(openInventory)) {
                    player.closeInventory();
                }
            }
        }
    }
}
