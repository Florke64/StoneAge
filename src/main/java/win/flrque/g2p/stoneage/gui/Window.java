package win.flrque.g2p.stoneage.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import win.flrque.g2p.stoneage.StoneAge;

public abstract class Window {

    private final StoneAge plugin;

    public Window() {
        plugin = StoneAge.getPlugin(StoneAge.class);
    }

    public boolean open(Player player) {
        player.closeInventory();

        if(player.isSleeping()) {
            return false;
        }

        plugin.getWindowManager().cacheWindow(player, this);

        return true;
    }

    public abstract void updateInventoryContent();

    public abstract Inventory getBukkitInventory();

    public abstract void onClick(ClickType clickType, Player player, InventoryPoint clickedPoint);

}
