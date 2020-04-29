/*
 * Copyright Go2Play.pl (c) 2020.
 * Program made for Go2Play Skyblock server. It's not allowed to re-distribute the code.
 * Author: FlrQue
 */

package win.flrque.g2p.stoneage.drop;

import org.bukkit.Material;
import org.bukkit.block.Dispenser;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import win.flrque.g2p.stoneage.StoneAge;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DropCalculator {

    private final StoneAge plugin;

    private final List<DropEntry> dropEntries = new ArrayList<>();

    private DropEntry primitiveDrop;
    private DropMultiplier dropMultiplier;

    private float totalWeight = 0;
    private float totalWeightWithMultiplier = 0;

    public DropCalculator() {
        plugin = StoneAge.getPlugin(StoneAge.class);

        this.primitiveDrop = new DropEntry(new ItemStack(Material.COBBLESTONE), 1.0f);

        calculateTotalWeight();
        calculateTotalWeightWithMultiplier(1.0f);
    }

    public void setDropMultiplier(DropMultiplier dropMultiplier) {
        this.dropMultiplier = dropMultiplier;
    }

    public DropMultiplier getDropMultiplier() {
        return dropMultiplier == null ? new DropMultiplier(1.0f, 2.0f) : dropMultiplier;
    }

    public void setPrimitiveDrop(DropEntry dropEntry) {
        primitiveDrop = dropEntry;
        calculateTotalWeight();
    }

    public int addDrop(DropEntry dropEntry) {
        dropEntries.add(dropEntry);
        calculateTotalWeight();

        return dropEntries.size();
    }

    private float calculateTotalWeight() {
        float weight = 0.0f;
        for(DropEntry drop : dropEntries)
            weight += drop.getChanceWeight();

        totalWeight = weight + primitiveDrop.getChanceWeight();

        return weight;
    }

    private float calculateTotalWeightWithMultiplier(float multiplier) {
        float weight = 0.0f;
        for(DropEntry drop : dropEntries) {
            if(drop.isMultipliable())
                weight += drop.getChanceWeight() * multiplier;
            else
                weight += drop.getChanceWeight();
        }

        totalWeightWithMultiplier = weight + primitiveDrop.getChanceWeight() * (primitiveDrop.isMultipliable()? multiplier : 1.0f);

        return weight;
    }

    public float getTotalWeightWithMultiplier() {
        return totalWeightWithMultiplier;
    }

    public float getTotalWeight() {
        return totalWeight;
    }

    public DropLoot calculateDrop(Player player, ItemStack tool, Dispenser stoneMachine) {
        //TODO: Check StoneMachine's configuration book inside its Inventory

        //No tool was used to break a block
        if(tool == null) return null;

        //TODO: Check pickaxe material (config: minimalPickaxe: <0; 2> = {wood, stone, iron})
        //Checking tool properties
        boolean hasSilkTouch = false;
        int fortuneLevel = 0;

        if(tool.hasItemMeta()) {
            for(Enchantment enchant : tool.getEnchantments().keySet()) {
                if(enchant.equals(Enchantment.SILK_TOUCH))
                    hasSilkTouch = true;

                else if(enchant.equals(Enchantment.LOOT_BONUS_BLOCKS))
                    fortuneLevel = tool.getEnchantments().get(enchant);
            }
        }

        //Calculating final drop
        final ItemStack finalDrop;
        final Random randomizer = new Random();

        DropEntry randomizedDropEntry = primitiveDrop;
        final float activeMultiplier = getDropMultiplier().getCurrentDropMultiplier();
        float luck = randomizer.nextFloat() * (totalWeight * activeMultiplier);
        System.out.println(": = = = = = = : ");
        System.out.println("luck: " + luck);
        for (int i = 0; i < dropEntries.size(); ++i) {
            System.out.println("item: " + dropEntries.get(i).getDropEntryIcon().getType());
            final boolean multipliable = dropEntries.get(i).isMultipliable();
            System.out.println("multipliable: " + multipliable);

            float itemChanceWeight = dropEntries.get(i).getChanceWeight();
            System.out.println("itemChanceWeight: " + itemChanceWeight);
            itemChanceWeight = dropEntries.get(i).getChanceWeight() * ((multipliable) ? activeMultiplier : 1.0f);
            System.out.println("itemChanceWeight: " + itemChanceWeight);

            luck -= itemChanceWeight;
            System.out.println("final luck: " + luck);

            if (luck <= 0.0f) {
                randomizedDropEntry = dropEntries.get(i);
                break;
            }
        }

        finalDrop = randomizedDropEntry.getDrop(hasSilkTouch, (randomizedDropEntry.equals(primitiveDrop))? 0 : fortuneLevel);

        return new DropLoot(randomizedDropEntry, finalDrop);
    }

    public DropEntry getPrimitiveDrop() {
        return primitiveDrop;
    }

    public List<DropEntry> getDropEntries() {
        return dropEntries;
    }
}
