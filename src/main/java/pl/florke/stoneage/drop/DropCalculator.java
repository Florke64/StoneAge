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
import org.bukkit.block.Dispenser;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.florke.stoneage.StoneAge;
import pl.florke.stoneage.database.playerdata.PlayerConfig;
import pl.florke.stoneage.database.playerdata.PlayerStats;
import pl.florke.stoneage.database.playerdata.PlayersData;
import pl.florke.stoneage.machine.ItemAutoSmelter;

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

    public DropMultiplier getDropMultiplier() {
        return dropMultiplier == null ? new DropMultiplier(1.0f, 2.0f) : dropMultiplier;
    }

    public void setDropMultiplier(DropMultiplier dropMultiplier) {
        this.dropMultiplier = dropMultiplier;
    }

    public void setPrimitiveDrop(DropEntry dropEntry) {
        primitiveDrop = dropEntry;
        calculateTotalWeight();
    }

    public void addDrop(DropEntry dropEntry) {
        dropEntries.put(dropEntry.getEntryName(), dropEntry);
        calculateTotalWeight();
    }

    private void calculateTotalWeight() {
        float weight = 0.0f;
        for (DropEntry drop : dropEntries.values())
            weight += drop.getChanceWeight();

        totalWeight = weight + primitiveDrop.getChanceWeight();
    }

    public float getTotalWeight() {
        return totalWeight;
    }

    public DropLoot calculateDrop(Player player, ItemStack tool, @Nullable Dispenser stoneMachine) {
        //No tool was used to break a block
        if (tool == null) return null;

        //Not applicable tool was used, means no drops
        if (!plugin.getApplicableTools().isApplicableTool(tool.getType())) {
            return null;
        }

        final int usedToolLevel = plugin.getApplicableTools().getToolLevel(tool);

        //Checking tool properties
        boolean hasSilkTouch = false;
        int fortuneLevel = 0;

        if (tool.hasItemMeta()) {
            for (Enchantment enchant : tool.getEnchantments().keySet()) {
                if (enchant.equals(Enchantment.SILK_TOUCH))
                    hasSilkTouch = true;

                else if (enchant.equals(Enchantment.FORTUNE))
                    fortuneLevel = tool.getEnchantments().get(enchant);
            }
        }

        //Calculating final drop
        final Random randomizer = new Random();

        final DropLoot dropLoot = new DropLoot();

        final PlayersData playerSetup = plugin.getPlayersData();
        final PlayerConfig dropConfig = playerSetup.getPersonalDropConfig(player.getUniqueId());
        final PlayerStats playerStats = playerSetup.getPlayerStoneMachineStats(player.getUniqueId());

        final ItemAutoSmelter autoSmelter = plugin.getStoneMachine().getItemSmelter();

        //Checks if cobble wasn't disabled by the player
        if (dropConfig.isDropping(primitiveDrop)) {
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
            if (playerStats.getMinerLvl() < dropEntry.getNeededMinerLevel()) {
                continue;
            }

            if (usedToolLevel < dropEntry.getNeededToolLevel()) {
                continue;
            }

            //Checks for player's personalised drop entry settings
            if (!dropConfig.isDropping(dropEntry)) {
                continue;
            }

            final float luck = randomizer.nextFloat() * totalWeight;

            final float itemChanceWeight = dropEntry.getChanceWeight();
            final float currentDropMultiplier;
            if (dropMultiplier.isActive()) {
                currentDropMultiplier = this.getDropMultiplier().getCurrentDropMultiplier();
            } else {
                currentDropMultiplier = getDropMultiplier().getDefaultDropMultiplier();
            }

            if (luck < itemChanceWeight * currentDropMultiplier) {
                ItemStack itemDrop = dropEntry.getDrop(hasSilkTouch, fortuneLevel);

                //Auto smelting feature
                if (stoneMachine != null && autoSmelter.getAutoSmeltingUsesLeft(stoneMachine) >= itemDrop.getAmount()) {
                    final ItemStack smelted = autoSmelter.getSmelted(stoneMachine, itemDrop);
                    if (smelted != null) {
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
        if (key.contentEquals(primitiveDrop.getEntryName()))
            return getPrimitiveDropEntry();
        return dropEntries.get(key);
    }

    public List<DropEntry> getDropEntries() {
        return new ArrayList<>(dropEntries.values());
    }
}
