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

package pl.florke.stoneage.machine;

import org.bukkit.NamespacedKey;
import org.bukkit.block.TileState;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import pl.florke.stoneage.StoneAge;
import pl.florke.stoneage.util.Message;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

public class ItemAutoSmelter {

    private final StoneAge plugin;

    public static final NamespacedKey AUTOSMELTER_KEY = new NamespacedKey(StoneAge.getPlugin(StoneAge.class), "autosmelter");

    // Equivalent of Math.pow(2, 24) = 2^24
    public static final int MAX_FUEL_CAPACITY = 1 << 24;

    private final List<FurnaceRecipe> smeltingRecipeList = new ArrayList<>();

    ItemAutoSmelter() {
        this.plugin = StoneAge.getPlugin(StoneAge.class);

        cacheSmeltingRecipes();
    }

    private void cacheSmeltingRecipes() {
        int index = 0;
        final Iterator<Recipe> recipeIterator = plugin.getServer().recipeIterator();

        while (recipeIterator.hasNext()) {
            final Recipe recipe = recipeIterator.next();
            if (!(recipe instanceof FurnaceRecipe smeltingRecipe))
                continue;

            this.smeltingRecipeList.add(smeltingRecipe);
            index++;
        }

        new Message("Cached $_1 smelting recipes for auto smelting feature.")
                .placeholder(1, Integer.toString(index)).log(Level.INFO);
    }

    // TODO: config to exclude certain drops from auto smelting
    public synchronized ItemStack getSmelted(@NotNull final TileState machineState, @NotNull final ItemStack itemToSmelt) {
        for (final FurnaceRecipe recipe : this.smeltingRecipeList) {
            final RecipeChoice input = recipe.getInputChoice();

            //Checking if input is similar to the item provided as argument
            if (input.test(itemToSmelt)) {
                final ItemStack smeltedItemStack = recipe.getResult();
                smeltedItemStack.setAmount(itemToSmelt.getAmount());

                // #getSmelted is called on drop calculate which is async
                //  It operates on block data so has to be run in tick
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        takeAutoSmeltingUse(machineState, itemToSmelt.getAmount());
                    }
                }.runTask(plugin);

                // TODO: This may return ItemStack[] because smelting can sometimes produce more stacks
                // For example if not enough fuel is available to smelt full stack.
                // This may also mean that (usage of) #takeAutoSmeltingUse() is bugged, but I don't want to check it now.
                return smeltedItemStack;
            }
        }

        return null;
    }

    public synchronized Integer getAutoSmeltingUsesLeft(@NotNull final TileState machineState) {
        if (!plugin.getStoneMachine().isStoneMachine(machineState))
            return -1;

        if (!hasAutoSmelting(machineState))
            return 0;

        final PersistentDataContainer machineData = machineState.getPersistentDataContainer();
        final Integer availableSmeltingUses = machineData.get(AUTOSMELTER_KEY, PersistentDataType.INTEGER);
        machineState.setBlockData(machineState.getBlockData());
        machineState.update();

        if (availableSmeltingUses == null)
            return -1;

        return availableSmeltingUses;
    }

    /**
     * Checks if given Stone Machine has enabled auto-smelting feature.
     * For actual auto-smelting uses left see {@link #getAutoSmeltingUsesLeft(TileState)}.
     *
     * @param machineState Stone Machine's {@link TileState} instance.
     * @return {@code true} if given Stone Machine has enabled auto-smelting.
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private synchronized boolean hasAutoSmelting(@NotNull final TileState machineState) {
        final PersistentDataContainer machineData = machineState.getPersistentDataContainer();
        machineState.setBlockData(machineState.getBlockData());
        machineState.update();

        if (!machineData.has(AUTOSMELTER_KEY))
            return false;

        if (machineData.get(AUTOSMELTER_KEY, PersistentDataType.INTEGER) instanceof Integer smelting)
            return smelting >= 0;

        return false;
    }

    public synchronized void takeAutoSmeltingUse(@NotNull final TileState machineState, final int usesToTake) {
        final int availableSmeltingUses = getAutoSmeltingUsesLeft(machineState);

        if (availableSmeltingUses < usesToTake)
            throw new IllegalArgumentException("Not enough uses left!");

        final PersistentDataContainer machineData = machineState.getPersistentDataContainer();

        final Integer newSmeltingUses = availableSmeltingUses - usesToTake;

        if (machineData.has(AUTOSMELTER_KEY))
            machineData.remove(AUTOSMELTER_KEY);

        machineData.set(AUTOSMELTER_KEY, PersistentDataType.INTEGER, newSmeltingUses);
        machineState.setBlockData(machineState.getBlockData());
        machineState.update();
    }

    public synchronized boolean addAutoSmeltingUse(@NotNull final TileState machineState, final int usesToAdd) {
        final int availableSmeltingUses = getAutoSmeltingUsesLeft(machineState);

        if (availableSmeltingUses >= MAX_FUEL_CAPACITY)
            return false;

        final PersistentDataContainer machineData = machineState.getPersistentDataContainer();
        final int newSmeltingUses = availableSmeltingUses + usesToAdd;

        if (machineData.has(AUTOSMELTER_KEY))
            machineData.remove(AUTOSMELTER_KEY);

        machineData.set(AUTOSMELTER_KEY, PersistentDataType.INTEGER, newSmeltingUses);
        machineState.setBlockData(machineState.getBlockData());
        machineState.update();

        return true;
    }

}
