package win.flrque.g2p.stoneage.gui;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.scheduler.BukkitRunnable;
import win.flrque.g2p.stoneage.StoneAge;

import java.util.ArrayList;
import java.util.List;

public abstract class Window {

    protected final StoneAge plugin;
    private final List<Player> users;

    protected final String windowTitle;

    protected Inventory inventory;

    public Window(final String windowName) {
        plugin = StoneAge.getPlugin(StoneAge.class);
        users = new ArrayList<>();

        windowTitle = windowName;
        inventory = Bukkit.createInventory(null, InventoryType.CHEST, windowTitle);
    }

    public boolean open(Player player) {
        player.closeInventory();

        if(player.isSleeping()) {
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

    public abstract void onClick(ClickType clickType, Player player, InventoryPoint clickedPoint);

}
