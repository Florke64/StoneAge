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
import org.jetbrains.annotations.Nullable;
import pl.florke.stoneage.StoneAge;
import pl.florke.stoneage.config.GeneralConfigReader;
import pl.florke.stoneage.database.playerdata.PlayerConfig;
import pl.florke.stoneage.database.playerdata.PlayerStats;
import pl.florke.stoneage.database.playerdata.PlayersData;
import pl.florke.stoneage.machine.ResourceSpawner;

import java.util.*;

public class DropCalculator {

    private final Random random = new Random();

    private final StoneAge plugin = StoneAge.getPlugin(StoneAge.class);

    private final DropMultiplier dropMultiplier = new DropMultiplier();
    private final DropEntryManager dropEntryManager = new DropEntryManager();
    private final ExperienceCalculator expCalculator = new ExperienceCalculator();

    public DropCalculator() {}

    public DropMultiplier getDropMultiplier() {
        return this.dropMultiplier;
    }

    public ExperienceCalculator getExpCalculator() {
        return this.expCalculator;
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
    public DropLoot calculateCustomDrop(Player player, ItemStack tool, @Nullable TileState machineState, Material brokenBlock) {
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

        final DropEntry dropResource = dropEntryManager.getDropResourcesEntries().get(brokenBlock);

        final PlayersData playerSetup = plugin.getPlayersData();
        final PlayerConfig dropConfig = playerSetup.getPersonalDropConfig(player.getUniqueId());
        final PlayerStats playerStats = playerSetup.getPlayerStoneMachineStats(player.getUniqueId());

        //Checks if cobble wasn't disabled by the player
        if (dropConfig.isDropping(dropResource)) {
            ItemStack resourceItemStack = dropResource.getDrop(hasSilkTouch, fortuneLevel);
            dropLoot.addLoot(dropResource, resourceItemStack);
        }

        final ResourceSpawner resourceSpawner = plugin.getStoneMachine().getResourceSpawner();
        for (DropEntry dropEntry : resourceSpawner.getResourceChildren(dropResource)) {
            // Verify requirements for this drop
            if (playerStats.getMinerLvl() < dropEntry.getNeededMinerLevel())
                continue;

            if (!dropEntry.getBlockMaterial().createBlockData().isPreferredTool(tool))
                continue;

            //Checks for player's personalised drop entry settings
            if (!dropConfig.isDropping(dropEntry))
                continue;


            float itemChanceWeight = dropEntry.getChanceWeight();
            if (dropEntry.isMultipliable())
                itemChanceWeight *= dropMultiplier.getCurrentDropMultiplier();

            if (random.nextFloat() < itemChanceWeight) {
                ItemStack itemDrop = dropEntry.getDrop(hasSilkTouch, fortuneLevel);
                dropLoot.addLoot(dropEntry, itemDrop.clone());
            }
        }

        //this means that auto-smelting is allowed in the config
        if (plugin.getStoneMachine().isHopperInputAllowed())
            dropLoot.applyAutoSmeltingFeature(machineState);

        return dropLoot;
    }

    public DropEntry calculateDropResource() {
        DropEntry dropResource = dropEntryManager.getDropResourcesEntries().values().stream().findFirst().orElse(null);

        for (DropEntry entry : dropEntryManager.getDropResourcesEntries().values()) {
            if (!plugin.getStoneMachine().getResourceSpawner().isRegisteredResource(entry))
                continue;

            final float dropMultiplierValue = entry.isMultipliable() && getDropMultiplier().isActive()?
                    getDropMultiplier().getCurrentDropMultiplier() : getDropMultiplier().getBaseDropMultiplier();

            float chanceWeight = entry.getChanceWeight();
            if (entry.isMultipliable())
                chanceWeight *= dropMultiplierValue;

            if (random.nextFloat() < chanceWeight)
                return entry;
        }

        return dropResource;
    }

    public float getChancePercentage(NamespacedKey drop) {
        final DropEntry dropEntry = dropEntryManager.getDropEntry(drop);
        if (dropEntry == null)
            return 0;

        return 100 * dropEntry.getChanceWeight();
    }

    public float getRealChancePercentage(NamespacedKey drop) {
        final DropCalculator calculator = plugin.getDropCalculator();
        final DropEntry dropEntry = dropEntryManager.getDropEntry(drop);
        final DropMultiplier dropMultiplier = calculator.getDropMultiplier();

        if (dropEntry == null)
            return 0;

        float chanceWeight = dropEntry.getChanceWeight();
        if (dropEntry.isMultipliable())
            chanceWeight *= dropMultiplier.getCurrentDropMultiplier();

        return (chanceWeight * 100);
    }

    public void reloadConfig(GeneralConfigReader generalConfig) {
        dropEntryManager.saveDefaultDrops();

        expCalculator.setMaximumMinerLevel(generalConfig.getMaxMinerLevel());

        //Reading 'DropEntry' configuration
        for (final DropEntry dropResourceEntry : dropEntryManager.readDropResourceEntries())
            dropEntryManager.addDropResource(dropResourceEntry);

        for (final DropEntry dropEntry : dropEntryManager.readCustomDropEntries())
            dropEntryManager.addCustomDrop(dropEntry);

        dropEntryManager.reloadConfig(generalConfig);
    }

    public DropEntryManager getDropEntryManager() {
        return dropEntryManager;
    }
}
