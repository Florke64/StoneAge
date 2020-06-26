/*
 * Copyright Go2Play.pl (c) 2020.
 * Program made for Go2Play Skyblock server. It's not allowed to re-distribute the code.
 * Author: FlrQue
 */

package win.flrque.g2p.stoneage.drop;

import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Dispenser;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import win.flrque.g2p.stoneage.StoneAge;

import java.util.*;

public class DropCalculator {

    private final StoneAge plugin;

    private final List<DropEntry> dropEntries = new ArrayList<>();
    private final Map<OfflinePlayer, PersonalDropConfig> personalDropConfigMap = new HashMap<>();

    private DropEntry primitiveDrop;
    private DropMultiplier dropMultiplier;

    private float totalWeight = 0;

    public DropCalculator() {
        plugin = StoneAge.getPlugin(StoneAge.class);

        this.primitiveDrop = new DropEntry(new ItemStack(Material.COBBLESTONE), 1.0f);

        calculateTotalWeight();
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

    public float getTotalWeight() {
        return totalWeight;
    }

    public DropLoot calculateDrop(Player player, ItemStack tool, @Nullable Dispenser stoneMachine) {
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
        final DropCalculator calculator = plugin.getDropCalculator();
        final Random randomizer = new Random();

        final DropLoot dropLoot = new DropLoot();

        //Checks if cobble wasn't disabled by the player
        if(getPersonalDropConfig(player).isDropping(primitiveDrop))
            dropLoot.addLoot(primitiveDrop, primitiveDrop.getDrop(hasSilkTouch, fortuneLevel));

        for (int i = 0; i < dropEntries.size(); ++i) {

            //Checks for player's personalised drop entry settings
            if(!getPersonalDropConfig(player).isDropping(dropEntries.get(i)))
                continue;

            final float luck = randomizer.nextFloat() * totalWeight;

            final float itemChanceWeight = dropEntries.get(i).getChanceWeight();
            final float currentDropMultiplier = calculator.getDropMultiplier().getCurrentDropMultiplier();

            if (luck <  itemChanceWeight * currentDropMultiplier) {
                final ItemStack itemDrop = dropEntries.get(i).getDrop(hasSilkTouch, fortuneLevel);

                dropLoot.addLoot(dropEntries.get(i), itemDrop);
            }
        }

        return dropLoot;
    }

    public DropEntry getPrimitiveDropEntry() {
        return primitiveDrop;
    }

    public List<DropEntry> getDropEntries() {
        return dropEntries;
    }

    public PersonalDropConfig getPersonalDropConfig(OfflinePlayer player) {
        if(!personalDropConfigMap.containsKey(player)) {
            createPersonalDropConfig(player);
        }

        return personalDropConfigMap.get(player);
    }

//    public void loadPersonalDropConfigs() {
//        //TODO: Load all from DataBase on server start
//    }

    private PersonalDropConfig createPersonalDropConfig(OfflinePlayer player) {
        final PersonalDropConfig config = new PersonalDropConfig(player);
        personalDropConfigMap.put(player, config);

        return personalDropConfigMap.get(player);
    }
}
