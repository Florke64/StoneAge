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

    private float totalWeight = 0;

    public DropCalculator() {
        plugin = StoneAge.getPlugin(StoneAge.class);

        this.primitiveDrop = new DropEntry(new ItemStack(Material.WOOL), 1.0f);

        calculateTotalWeight();
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
        final Random random = new Random();

        DropEntry randomizedDropEntry = primitiveDrop;
        float luck = random.nextFloat() * totalWeight;
        for (int i = 0; i < dropEntries.size(); ++i)
        {
            luck -= dropEntries.get(i).getChanceWeight();
            if (luck <= 0.0f)
            {
                randomizedDropEntry = dropEntries.get(i);
                break;
            }
        }

        finalDrop = randomizedDropEntry.getDrop(hasSilkTouch, fortuneLevel);

        return new DropLoot(finalDrop, randomizedDropEntry.calculateFinalExpValue());
    }



}
