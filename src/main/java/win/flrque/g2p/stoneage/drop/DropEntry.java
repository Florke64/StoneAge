/*
 * Copyright Go2Play.pl (c) 2020.
 * Program made for Go2Play Skyblock server. It's not allowed to re-distribute the code.
 * Author: FlrQue
 */

package win.flrque.g2p.stoneage.drop;

import org.bukkit.inventory.ItemStack;
import win.flrque.g2p.stoneage.StoneAge;

import java.util.Random;

public class DropEntry {

    private final StoneAge plugin;

    private final ItemStack defaultItemStack;
    private final float chanceWeight;

    private int minAmount;
    private int maxAmount;
    private boolean ignoreFortuneEnchant = false;

    private boolean multipliable = true;

    private int minExp;
    private int maxExp;

    private ItemStack silkTouchItemStack;

    public DropEntry(ItemStack itemStack, float weight) {
        plugin = StoneAge.getPlugin(StoneAge.class);

        chanceWeight = weight;
        defaultItemStack = itemStack;

        minAmount = 1;
        maxAmount = minAmount;

        minExp = 1;
        maxExp = 5;

        silkTouchItemStack = itemStack;
    }

    public ItemStack getDrop(boolean silkTouch, int fortuneLevel) {
        final ItemStack itemStack;
        if(silkTouch) {
            //TODO: Check without cloning
            itemStack = silkTouchItemStack.clone();
            itemStack.setAmount(1);

            return itemStack;
        }

        itemStack = defaultItemStack.clone();
        itemStack.setAmount(calculateFinalAmount(fortuneLevel));

        return itemStack;
    }

    public ItemStack getDropEntryIcon() {
        return defaultItemStack.clone();
    }

    public void setIgnoreFortuneEnchant(boolean ignoreFortuneEnchant) {
        this.ignoreFortuneEnchant = ignoreFortuneEnchant;
    }

    public boolean isIgnoreFortuneEnchant() {
        return ignoreFortuneEnchant;
    }

    public void setMinAmount(int amount) {
        minAmount = (amount < 1)? 1 : minAmount;
    }

    public int getMinAmount() {
        return minAmount;
    }

    public void setMaxAmount(int amount) {
        maxAmount = (amount > minAmount)? amount : minAmount;
    }

    public int getMaxAmount() {
        return maxAmount;
    }

    public void setSilkTouchItemStack(ItemStack itemStack) {
        silkTouchItemStack = itemStack;
    }

    public float getChanceWeight() {
        return chanceWeight;
    }

    public int calculateFinalAmount(int fortuneLevel) {
        final Random random = new Random();
        int amount = minAmount==maxAmount? minAmount : random.nextInt(maxAmount - minAmount) + minAmount;

        if(isIgnoreFortuneEnchant())
            return amount;

        for(int i = 0; i<fortuneLevel; i++) {
            if(random.nextFloat() <= 0.35f)
                amount += 1;
        }

        return amount;
    }

    public int getMinimalExp() {
        return minExp;
    }

    public int getMaximalExp() {
        return maxExp;
    }

    public void setMinimalExp(int exp) {
        minExp = (exp<0)? 0 : exp;
    }

    public void setMaximalExp(int exp) {
        maxExp = (exp<minExp)? minExp : exp;
    }

    public int calculateFinalExpValue() {
        final Random random = new Random();

        return random.nextInt(maxExp - minExp) + (minAmount);
    }

    public boolean isMultipliable() {
        return multipliable;
    }

    public void setMultipliable(boolean multipliable) {
        this.multipliable = multipliable;
    }

}
