/*
 * Copyright Go2Play.pl (c) 2020.
 * Program made for Go2Play Skyblock server. It's not allowed to re-distribute the code.
 * Author: FlrQue
 */

package win.flrque.g2p.stoneage.drop;

import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class DropLoot {

    private final Map<DropEntry, ItemStack> loots = new HashMap<>();

    private int totalExp = 0;

    public void addLoot(DropEntry dropEntry, ItemStack itemStack) {
        if(dropEntry.getMaximalExp() > 0)
            totalExp += dropEntry.calculateFinalExpValue();

        loots.put(dropEntry, itemStack);
    }

    public ItemStack getItemLoot(DropEntry entry) {
        return loots.get(entry);
    }

    public int getExp() {
        return totalExp;
    }

    public Collection<DropEntry> getActiveDropEntries() {
        return loots.keySet();
    }

    public int getAmountLooted(DropEntry key) {
        return loots.get(key).getAmount();
    }

}
