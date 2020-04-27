package win.flrque.g2p.stoneage.drop;

import org.bukkit.inventory.ItemStack;

public class DropLoot {

    private final DropEntry dropEntry;

    private final ItemStack itemStack;
    private final int exp;

    public DropLoot(final DropEntry dropEntry, final ItemStack itemStack) {
        this.dropEntry = dropEntry;

        this.itemStack = itemStack;

        if(dropEntry.getMaximalExp() > 0)
            this.exp = dropEntry.calculateFinalExpValue();
        else this.exp = 0;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public int getExp() {
        return exp;
    }

}
