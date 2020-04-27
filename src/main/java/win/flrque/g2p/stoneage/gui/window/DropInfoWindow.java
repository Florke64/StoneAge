package win.flrque.g2p.stoneage.gui.window;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import win.flrque.g2p.stoneage.drop.DropCalculator;
import win.flrque.g2p.stoneage.drop.DropEntry;
import win.flrque.g2p.stoneage.gui.InventoryPoint;
import win.flrque.g2p.stoneage.gui.Window;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class DropInfoWindow extends Window  {

    private final Player windowContentOwner;

    //TODO: Support for pagination
    public DropInfoWindow(Player owner) {
        super(ChatColor.translateAlternateColorCodes('&', "&7&lSTONIARKA &8&l> &5&lDROP INFO"));
        windowContentOwner = owner;
    }

    @Override
    public void updateInventoryContent() {
        final DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(2);
        df.setMinimumFractionDigits(2);

        final DropCalculator calculator = plugin.getDropCalculator();

        for(int i=0; i<=calculator.getDropEntries().size(); i++) {
            final DropEntry drop;
            if(i == calculator.getDropEntries().size()) drop = calculator.getPrimitiveDrop();
            else drop = calculator.getDropEntries().get(i);

            final float dropChance = (100 * drop.getChanceWeight()) / calculator.getTotalWeight();

            final ItemStack icon = drop.getDropEntryIcon();
            icon.setAmount(1);

            final ItemMeta meta = icon.getItemMeta();
            meta.setDisplayName(ChatColor.GREEN + icon.getType().toString() + ChatColor.GOLD +" ("+ df.format(dropChance) +"%)");
            final List<String> lore = new ArrayList<>();

            icon.setItemMeta(meta);

            inventory.setItem(i, icon);
        }
    }

    @Override
    public boolean open(Player player) {
        if(!super.open(player)) {
            player.sendMessage("Nie udało się otworzyć okna dropu!");
            return false;
        }

        player.openInventory(inventory);

        return true;
    }

    @Override
    public void onClick(ClickType clickType, Player player, InventoryPoint clickedPoint) {

    }

}
