/*
 * Copyright Go2Play.pl (c) 2020.
 * Program made for Go2Play Skyblock server. It's not allowed to re-distribute the code.
 * Author: FlrQue
 */

package win.flrque.g2p.stoneage.drop;

import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class DropLoot {

    private final Map<ItemStack, Integer> loots = new HashMap<>();

    public void addLoot(DropEntry dropEntry, ItemStack itemStack) {
        final int exp;

        if(dropEntry.getMaximalExp() > 0)
            exp = dropEntry.calculateFinalExpValue();
        else exp = 0;

        loots.put(itemStack, exp);
    }

    public Set<ItemStack> getLoots() {
        return loots.keySet();
    }

    public int getExp(ItemStack itemStack) {
        return loots.get(itemStack);
    }

}
