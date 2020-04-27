package win.flrque.g2p.stoneage.gui.window;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Dispenser;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import win.flrque.g2p.stoneage.gui.InventoryPoint;
import win.flrque.g2p.stoneage.gui.Window;

public class StoneMachineWindow extends Window {

    private final Player windowOwner;

    private final Dispenser stoneMachine;

    public StoneMachineWindow(Player owner, Dispenser stoneMachine) {
        super(ChatColor.translateAlternateColorCodes('&', "&7&lSTONIARKA &8&l> &5&lOPCJE"));

        windowOwner = owner;
        this.stoneMachine = stoneMachine;
    }

    public Dispenser getStoneMachine() {
        return stoneMachine;
    }

    public Player getWindowOwner() {
        return windowOwner;
    }

    @Override
    public boolean open(Player player) {
        if(!super.open(player)) {
            player.sendMessage("Nie udało się otworzyć okna stoniarki!");
            return false;
        }

        player.openInventory(inventory);

        return true;
    }

    @Override
    public void updateInventoryContent() {
        final ItemStack dummy = new ItemStack(Material.VINE);

        for(int i=0; i<inventory.getSize(); i++) {
            inventory.setItem(i, dummy);
        }
    }

    @Override
    public void onClick(ClickType clickType, Player player, InventoryPoint clickedPoint) {
        if(clickType.equals(ClickType.LEFT)) {
            player.sendMessage(clickedPoint.toString());
        }
    }



}
