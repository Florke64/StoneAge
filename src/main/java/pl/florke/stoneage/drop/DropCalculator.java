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
import org.bukkit.block.TileState;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.florke.stoneage.StoneAge;
import pl.florke.stoneage.database.playerdata.PlayerConfig;
import pl.florke.stoneage.database.playerdata.PlayerStats;
import pl.florke.stoneage.database.playerdata.PlayersData;
import pl.florke.stoneage.machine.ResourceSpawner;
import pl.florke.stoneage.util.Message;

import java.util.*;
import java.util.logging.Level;

public class DropCalculator {

    private final Random random = new Random();

    private final StoneAge plugin = StoneAge.getPlugin(StoneAge.class);

    private final LinkedHashMap<NamespacedKey, DropEntry> dropEntries = new LinkedHashMap<>();
    private final LinkedHashMap<Material, DropEntry> primitiveEntries = new LinkedHashMap<>();

    private DropMultiplier dropMultiplier;

    private float totalDropsWeight = 0;
    private float totalResourcesWeight = 0;

    public DropCalculator() {
        calculateTotalWeight();
    }

    public DropMultiplier getDropMultiplier() {
        return dropMultiplier != null?
                this.dropMultiplier : new DropMultiplier(1.0f, 2.0f);
    }

    public void setDropMultiplier(DropMultiplier dropMultiplier) {
        this.dropMultiplier = dropMultiplier;
    }

    public void addPrimitiveDrop(DropEntry dropEntry) {
        primitiveEntries.put(dropEntry.getBlockMaterial(), dropEntry);
        calculateTotalWeight();
    }

    public void addCustomDrop(DropEntry dropEntry) {
        dropEntries.put(dropEntry.getKey(), dropEntry);
        calculateTotalWeight();
    }

    private void calculateTotalWeight() {
        float dropWeight = 0.0f;
        float resourcesWeight = 0.0f;

        for (DropEntry drop : dropEntries.values())
            dropWeight += drop.getChanceWeight();

        new Message("Drop weight: " + dropWeight).log(Level.INFO);

        for (DropEntry drop : primitiveEntries.values())
            resourcesWeight += drop.getChanceWeight();

        totalDropsWeight = dropWeight;
        totalResourcesWeight = resourcesWeight;
    }

    public float getTotalDropsWeight() {
        return totalDropsWeight;
    }

    public float getTotalResourcesWeight() {
        return totalResourcesWeight;
    }

/**
 * Calculates the drop loot for a player based on the tool used and the stone machine.
 *
 * @param player the player who is breaking the block
 * @param tool the tool used by the player to break the block
 * @param machineState the stone machine involved, can be null
 * @return a DropLoot object containing the items and experience to be dropped
 *         or null if no applicable tool was used
 */
    public DropLoot calculateDrop(Player player, ItemStack tool, @Nullable TileState machineState, Material brokenBlock) {
        //No tool was used to break a block
        if (tool == null)
            return null;

        //Checking tool properties
        boolean hasSilkTouch = false;
        int fortuneLevel = 0;

        if (tool.hasItemMeta()) {
            if (tool.getEnchantments().containsKey(Enchantment.SILK_TOUCH))
                hasSilkTouch = true;
            else if (tool.getEnchantments().containsKey(Enchantment.FORTUNE))
                fortuneLevel = tool.getEnchantments().get(Enchantment.FORTUNE);
        }

        //Calculating final drop
        final DropLoot dropLoot = new DropLoot();

        final DropEntry primitiveDrop = getPrimitiveDropEntries().get(brokenBlock);

        final PlayersData playerSetup = plugin.getPlayersData();
        final PlayerConfig dropConfig = playerSetup.getPersonalDropConfig(player.getUniqueId());
        final PlayerStats playerStats = playerSetup.getPlayerStoneMachineStats(player.getUniqueId());

        //Checks if cobble wasn't disabled by the player
        if (dropConfig.isDropping(primitiveDrop)) {
            ItemStack primitiveItemStack = primitiveDrop.getDrop(hasSilkTouch, fortuneLevel);
            dropLoot.addLoot(primitiveDrop, primitiveItemStack);
        }

        final ResourceSpawner resourceSpawner = plugin.getStoneMachine().getResourceSpawner();
        for (DropEntry dropEntry : resourceSpawner.getResourceChildren(primitiveDrop)) {
            // Verify requirements for this drop
            if (playerStats.getMinerLvl() < dropEntry.getNeededMinerLevel())
                continue;

            if (!dropEntry.getBlockMaterial().createBlockData().isPreferredTool(tool))
                continue;

            //Checks for player's personalised drop entry settings
            if (!dropConfig.isDropping(dropEntry))
                continue;

            final float luck = random.nextFloat() * getTotalDropsWeight();

            final float itemChanceWeight = dropEntry.getChanceWeight() / getTotalDropsWeight();
            final float currentDropMultiplier = dropEntry.isMultipliable() && getDropMultiplier().isActive()?
                    getDropMultiplier().getCurrentDropMultiplier() : getDropMultiplier().getDefaultDropMultiplier();

            if (luck < itemChanceWeight * currentDropMultiplier) {
                ItemStack itemDrop = dropEntry.getDrop(hasSilkTouch, fortuneLevel);
                dropLoot.addLoot(dropEntry, itemDrop.clone());
            }
        }

        //this means that auto-smelting is allowed in the config
        if (plugin.getStoneMachine().isHopperInputAllowed())
            dropLoot.applyAutoSmeltingFeature(machineState);

        return dropLoot;
    }

    public DropEntry calculatePrimitive() {
        DropEntry defaultPrimitiveEntry = primitiveEntries.values().stream().findFirst().orElse(null);

        for (DropEntry entry : getPrimitiveDropEntries().values()) {
            if (!plugin.getStoneMachine().getResourceSpawner().isRegisteredResource(entry))
                continue;

            final float luck = random.nextFloat() * getTotalResourcesWeight();
            final float itemChanceWeight = entry.getChanceWeight() / getTotalResourcesWeight();

            final float dropMultiplierValue = entry.isMultipliable() && getDropMultiplier().isActive()?
                    getDropMultiplier().getCurrentDropMultiplier() : getDropMultiplier().getDefaultDropMultiplier();

            if (luck < itemChanceWeight * dropMultiplierValue)
                return entry;
        }

        return defaultPrimitiveEntry;
    }

    public LinkedHashMap<Material, DropEntry> getPrimitiveDropEntries() {
        return new LinkedHashMap<>(primitiveEntries);
    }

    @Nullable
    public DropEntry getDropEntry(@NotNull NamespacedKey key) {
        Optional<DropEntry> dropEntry = Optional.empty();

        if (isPrimitiveDrop(key))
            dropEntry = primitiveEntries.values().stream().filter(entry -> entry.getKey().equals(key)).findFirst();

        if (!isPrimitiveDrop(key) || dropEntry.isEmpty())
            return dropEntries.get(key);

        return dropEntry.get();
    }

    public List<DropEntry> getDropEntries() {
        return new ArrayList<>(dropEntries.values());
    }

    public boolean isPrimitiveDrop(final NamespacedKey key) {
        return primitiveEntries.values().stream().anyMatch(primitiveEntry -> primitiveEntry.getKey().equals(key));
    }

    public boolean isPrimitiveDrop(final DropEntry entry) {
        return primitiveEntries.values().stream().anyMatch(primitiveEntry -> primitiveEntry.equals(entry));
    }

    public boolean isPrimitiveDrop(final Material material) {
        return primitiveEntries.containsKey(material);
    }
}
