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

    private String entryName;
    private String customName;

    private final ItemStack defaultItemStack;
    private final float chanceWeight;

    private int minAmount;
    private int maxAmount;
    private boolean ignoreFortuneEnchant = false;

    private boolean multipliable = true;

    private int minExp;
    private int maxExp;

    private int minerExp;

    private int neededMinerLevel;
    private int neededToolLevel;

    private ItemStack silkTouchItemStack;
    //TODO: Store type of item to reduce ItemStack#getType() calls count

    public DropEntry(String entryName, ItemStack itemStack, float weight) {
        plugin = StoneAge.getPlugin(StoneAge.class);

        this.entryName = entryName;

        chanceWeight = weight;
        defaultItemStack = itemStack;

        minAmount = 1;
        maxAmount = minAmount;

        minExp = 1;
        maxExp = 5;

        minerExp = 0;

        neededMinerLevel = 0;
        neededToolLevel = 1;

        silkTouchItemStack = itemStack;
    }

    public ItemStack getDrop(boolean silkTouch, int fortuneLevel) {
        final ItemStack itemStack;
        if (silkTouch) {
            //TODO: Check without cloning
            itemStack = silkTouchItemStack.clone();
            itemStack.setAmount(1);

            return itemStack;
        }

        itemStack = defaultItemStack.clone();
        itemStack.setAmount(calculateFinalAmount(fortuneLevel));

        return itemStack;
    }

    public String getEntryName() {
        return entryName;
    }

    public String getCustomName() {
        return customName;
    }

    public void setCustomName(String customName) {
        this.customName = customName;
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
        this.minAmount = (amount < 1) ? 1 : amount;
    }

    public int getMinAmount() {
        return this.minAmount;
    }

    public void setMaxAmount(int amount) {
        this.maxAmount = (amount > this.minAmount) ? amount : this.minAmount;
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
        int amount = minAmount == maxAmount ? minAmount : random.nextInt(maxAmount - minAmount) + minAmount;

        if (isIgnoreFortuneEnchant())
            return amount;

        for (int i = 0; i < fortuneLevel; i++) {
            if (random.nextFloat() <= 0.35f)
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
        minExp = (exp < 0) ? 0 : exp;
    }

    public void setMaximalExp(int exp) {
        maxExp = (exp < minExp) ? minExp : exp;
    }

    public int calculateFinalExpValue() {
        final Random random = new Random();

        return random.nextInt(maxExp - minExp) + (minAmount);
    }

    public int getNeededMinerLevel() {
        return neededMinerLevel;
    }

    public void setNeededMinerLevel(int neededMinerLevel) {
        this.neededMinerLevel = neededMinerLevel;
    }

    public int getNeededToolLevel() {
        return neededToolLevel;
    }

    public void setNeededToolLevel(int neededToolLevel) {
        this.neededToolLevel = neededToolLevel;
    }

    public boolean isMultipliable() {
        return multipliable;
    }

    public void setMultipliable(boolean multipliable) {
        this.multipliable = multipliable;
    }

    public int getMinerExp() {
        return minerExp;
    }

    public void setMinerExp(int minerExp) {
        this.minerExp = minerExp;
    }
}
