package win.flrque.g2p.stoneage.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import win.flrque.g2p.stoneage.StoneAge;

import java.util.ArrayList;
import java.util.List;

public abstract class Window {

    private final StoneAge plugin;
    private final List<Player> users;

    public Window() {
        plugin = StoneAge.getPlugin(StoneAge.class);
        users = new ArrayList<>();
    }

    public boolean open(Player player) {
        player.closeInventory();

        if(player.isSleeping()) {
            return false;
        }

        plugin.getWindowManager().cacheWindow(player, this);
        users.add(player);

        return true;
    }

    public List<Player> getUsers() {
        return users;
    }

    public abstract void updateInventoryContent();

    public abstract Inventory getBukkitInventory();

    public abstract void onClick(ClickType clickType, Player player, InventoryPoint clickedPoint);

}
