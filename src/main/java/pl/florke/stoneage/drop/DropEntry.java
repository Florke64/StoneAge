/*
 * @Florke64 <Daniel Chojnacki>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package pl.florke.stoneage.drop;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

/* Objects of this class represent possible drop's properties
 * like the ItemStack min/max amount, min/max exp, chance etc. */
public class DropEntry {

    private final Random random = new Random();

    private final EntryType entryType;
    private Material blockMaterial;

    private final ItemStack defaultDrop;
    private ItemStack silkDrop;
    //TODO: Store type of item to reduce ItemStack#getType() calls count

    private final NamespacedKey entryKey;
    private final float chanceWeight;

    private boolean ignoreFortune = false;
    private boolean multipliable = true;

    private String customName;
    private int minAmount;
    private int maxAmount;

    private int minExp;
    private int maxExp;

    private int minerExp;

    private int neededMinerLevel;

    public DropEntry(EntryType category, NamespacedKey key, ItemStack itemStack, float weight) {
        entryType = category;
        blockMaterial = Material.STONE;

        defaultDrop = itemStack;
        silkDrop = itemStack;

        entryKey = key;
        chanceWeight = weight;

        minAmount = 1;
        maxAmount = minAmount; // Initially the same; can be set with #setMaxAmount() - only values greater than 0!

        minExp = 1;
        maxExp = 5;

        minerExp = 0;

        neededMinerLevel = 0;
    }

    public ItemStack getDrop(boolean silkTouch, int fortuneLevel) {
        final ItemStack itemStack;
        if (silkTouch) {
            itemStack = silkDrop.clone();
            itemStack.setAmount(1);

            return itemStack;
        }

        itemStack = defaultDrop.clone();
        itemStack.setAmount(calculateFinalAmount(fortuneLevel));

        return itemStack;
    }

    /**
     * Returns the id name of the entry.
     *
     * @deprecated Use {@link #getKey()} instead
     */
    @Deprecated
    public String getEntryId() {
        return entryKey.getKey();
    }

    public EntryType getEntryType() {
        return entryType;
    }

    public NamespacedKey getKey() {
        return entryKey;
    }

    public String getCustomName() {
        return customName;
    }

    public void setCustomName(String customName) {
        this.customName = customName;
    }

    public ItemStack getDropEntryIcon() {
        return defaultDrop.clone();
    }

    public boolean isIgnoreFortune() {
        return ignoreFortune;
    }

    public void setIgnoreFortune(boolean ignoreFortune) {
        this.ignoreFortune = ignoreFortune;
    }

    public int getMinAmount() {
        return this.minAmount;
    }

    public void setMinAmount(int amount) {
        this.minAmount = amount;
    }

    public int getMaxAmount() {
        return maxAmount;
    }

    public void setMaxAmount(int amount) {
        this.maxAmount = Math.max(amount, this.minAmount);
    }

    public void setSilkDrop(ItemStack itemStack) {
        silkDrop = itemStack;
    }

    public float getChanceWeight() {
        return chanceWeight;
    }

    public int calculateFinalAmount(int fortuneLevel) {
        int amount = minAmount == maxAmount ? minAmount : random.nextInt(maxAmount - minAmount) + minAmount;

        if (isIgnoreFortune())
            return amount;

        for (int i = 0; i < fortuneLevel; i++)
            if (random.nextFloat() <= 0.35f)
                amount += 1;

        return amount;
    }

    public int getMinimalExp() {
        return minExp;
    }

    public void setMinimalExp(int exp) {
        minExp = exp;
    }

    public int getMaximalExp() {
        return maxExp;
    }

    public void setMaximalExp(int exp) {
        maxExp = exp;
    }

    public int calculateFinalExpValue() {
        if (maxExp - minExp <= 0)
            return 1;

        return random.nextInt(maxExp - minExp) + (minExp);
    }

    public int getNeededMinerLevel() {
        return neededMinerLevel;
    }

    public void setNeededMinerLevel(int neededMinerLevel) {
        this.neededMinerLevel = neededMinerLevel;
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

    public void setBlockMaterial(Material blockMaterial) {
        this.blockMaterial = blockMaterial;
    }

    public Material getBlockMaterial() {
        return blockMaterial;
    }

    public enum EntryType {
        CUSTOM_DROP("drops", "drop_"),
        RESOURCE_DROP("drops/resources", "resource_");

        private final String path;
        private final String prefix;

        EntryType(final String path, final String namespacePrefix) {
            this.path = path;
            this.prefix = namespacePrefix;
        }

        public String getPath() {
            return path;
        }

        public String getPrefix() {
            return prefix;
        }
    }
}
