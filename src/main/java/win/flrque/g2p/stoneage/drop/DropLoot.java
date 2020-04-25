package win.flrque.g2p.stoneage.drop;

import org.bukkit.inventory.ItemStack;

public class DropLoot {

    private final ItemStack itemStack;
    private final int exp;

    public DropLoot(final ItemStack itemStack, int exp) {
        this.itemStack = itemStack;
        this.exp = exp;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public int getExp() {
        return exp;
    }

}
