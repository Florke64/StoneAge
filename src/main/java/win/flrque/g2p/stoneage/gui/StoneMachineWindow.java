package win.flrque.g2p.stoneage.gui;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;

public class StoneMachineWindow extends Window {

    private static final String WINDOW_TITLE_RAW = "&7&lSTONIARKA &8&l> &5&lOPCJE";
    public static final String WINDOW_TITLE = ChatColor.translateAlternateColorCodes('&', WINDOW_TITLE_RAW);

    private final Player windowOwner;
    private final Inventory inventory;

    public StoneMachineWindow(Player owner) {
        windowOwner = owner;

        inventory = Bukkit.createInventory(null, InventoryType.CHEST, WINDOW_TITLE);
    }

    public Player getWindowOwner() {
        return windowOwner;
    }

    @Override
    public boolean open(Player player) {
        if(!super.open(player)) {
            player.sendMessage("Nie udało się otworzyć okienka!");
            return false;
        }

        player.openInventory(inventory);

        return true;
    }

    @Override
    public void updateInventoryContent() {

    }

    @Override
    public Inventory getBukkitInventory() {
        return inventory;
    }

    @Override
    public void onClick(ClickType clickType, Player player, InventoryPoint clickedPoint) {

    }

}
