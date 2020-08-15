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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import win.flrque.g2p.stoneage.StoneAge;
import win.flrque.g2p.stoneage.database.playerdata.PlayerConfig;
import win.flrque.g2p.stoneage.database.playerdata.PlayerStats;
import win.flrque.g2p.stoneage.database.playerdata.PlayersData;
import win.flrque.g2p.stoneage.machine.ItemAutoSmelter;

import java.util.*;

public class DropCalculator {

    private final StoneAge plugin;

    private final Map<String, DropEntry> dropEntries = new LinkedHashMap<>();

    private DropEntry primitiveDrop;
    private DropMultiplier dropMultiplier;

    private float totalWeight = 0;

    public DropCalculator() {
        plugin = StoneAge.getPlugin(StoneAge.class);

        this.primitiveDrop = new DropEntry("primitive_drop", new ItemStack(Material.COBBLESTONE), 1.0f);

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
        dropEntries.put(dropEntry.getEntryName(), dropEntry);
        calculateTotalWeight();

        return dropEntries.size();
    }

    private float calculateTotalWeight() {
        float weight = 0.0f;
        for(DropEntry drop : dropEntries.values())
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

        //Not applicable tool was used, means no drops
        if(!plugin.getApplicableTools().isApplicableTool(tool.getType())) {
            return null;
        }

        final int usedToolLevel = plugin.getApplicableTools().getToolLevel(tool);

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
        final Random randomizer = new Random();

        final DropLoot dropLoot = new DropLoot();

        final PlayersData playerSetup = plugin.getPlayerSetup();
        final PlayerConfig dropConfig = playerSetup.getPersonalDropConfig(player.getUniqueId());
        final PlayerStats playerStats = playerSetup.getPlayerStoneMachineStats(player.getUniqueId());

        final ItemAutoSmelter autoSmelter = plugin.getStoneMachine().getItemSmelter();

        //Checks if cobble wasn't disabled by the player
        if(dropConfig.isDropping(primitiveDrop)) {
            ItemStack primitiveItemStack = primitiveDrop.getDrop(hasSilkTouch, fortuneLevel);

            //TODO: Autosmelting primitive drop
//            if(autoSmelter.getAutoSmeltingUsesLeft(stoneMachine) >= 1) {
//                final ItemStack smelted = autoSmelter.getSmelted(stoneMachine, primitiveItemStack);
//                if(smelted != null) {
//                    primitiveItemStack = smelted;
//                }
//            }

            dropLoot.addLoot(primitiveDrop, primitiveItemStack);
        }

        for (DropEntry dropEntry : dropEntries.values()) {

            //Check for requirements for this drop
            if(playerStats.getMinerLvl() < dropEntry.getNeededMinerLevel()) {
                continue;
            }

            if(usedToolLevel < dropEntry.getNeededToolLevel()) {
                continue;
            }

            //Checks for player's personalised drop entry settings
            if(!dropConfig.isDropping(dropEntry)) {
                continue;
            }

            final float luck = randomizer.nextFloat() * totalWeight;

            final float itemChanceWeight = dropEntry.getChanceWeight();
            final float currentDropMultiplier = this.getDropMultiplier().getCurrentDropMultiplier();

            if (luck <  itemChanceWeight * currentDropMultiplier) {
                ItemStack itemDrop = dropEntry.getDrop(hasSilkTouch, fortuneLevel);

                //Auto smelting feature
                if(autoSmelter.getAutoSmeltingUsesLeft(stoneMachine) >= itemDrop.getAmount()) {
                    final ItemStack smelted = autoSmelter.getSmelted(stoneMachine, itemDrop);
                    if(smelted != null) {
                        itemDrop = smelted;
                    }
                }

                dropLoot.addLoot(dropEntry, itemDrop);
            }
        }

        return dropLoot;
    }

    public DropEntry getPrimitiveDropEntry() {
        return primitiveDrop;
    }

    public DropEntry getDropEntry(@NotNull String key) {
        if(key.contentEquals(primitiveDrop.getEntryName()))
            return getPrimitiveDropEntry();
        return dropEntries.get(key);
    }

    public List<DropEntry> getDropEntries() {
        final List<DropEntry> dropEntryList = new ArrayList<>();
        dropEntryList.addAll(dropEntries.values());

        return dropEntryList;
    }
}
