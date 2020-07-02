/*
 * Copyright Go2Play.pl (c) 2020.
 * Program made for Go2Play Skyblock server. It's not allowed to re-distribute the code.
 * Author: FlrQue
 */

package win.flrque.g2p.stoneage.drop;

import org.bukkit.inventory.ItemStack;

import java.util.*;

public class DropLoot {

    private final Map<ItemStack, Integer> loots = new HashMap<>();
    private final List<DropEntry> activeDropEntries = new LinkedList<>();

    public void addLoot(DropEntry dropEntry, ItemStack itemStack) {
        final int exp;

        if(dropEntry.getMaximalExp() > 0)
            exp = dropEntry.calculateFinalExpValue();
        else exp = 0;

        loots.put(itemStack, exp);
        activeDropEntries.add(dropEntry);
    }

    public Set<ItemStack> getLoots() {
        return loots.keySet();
    }

    public int getExp(ItemStack itemStack) {
        return loots.get(itemStack);
    }

    public Collection<DropEntry> getActiveDropEntries() {
        return activeDropEntries;
    }

}
