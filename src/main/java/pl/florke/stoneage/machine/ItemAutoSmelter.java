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
import org.bukkit.block.Dispenser;
import org.bukkit.block.TileState;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
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

        new Message("Cached " + index + " smelting recipes for auto smelting feature.").log(Level.INFO);
    }

    public ItemStack getSmelted(@NotNull final Dispenser stoneMachine, @NotNull final ItemStack itemToSmelt) {
        for (final FurnaceRecipe recipe : this.smeltingRecipeList) {
            final RecipeChoice input = recipe.getInputChoice();

            //Checking if input is similar to the item provided as argument
            if (input.test(itemToSmelt)) {
                final ItemStack smeltedItemStack = recipe.getResult();
                smeltedItemStack.setAmount(itemToSmelt.getAmount());

                takeAutoSmeltingUse(stoneMachine, itemToSmelt.getAmount());

                return smeltedItemStack;
            }
        }

        return null;
    }

    public int getAutoSmeltingUsesLeft(@NotNull final TileState machineState) {
        if (!plugin.getStoneMachine().isStoneMachine(machineState))
            return -1;

        if (!hasAutoSmelting(machineState))
            return 0;

        final PersistentDataContainer machineData = machineState.getPersistentDataContainer();
        final Integer availableSmeltingUses = machineData.get(AUTOSMELTER_KEY, PersistentDataType.INTEGER);

        if (availableSmeltingUses == null)
            return -1;

        return availableSmeltingUses;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean hasAutoSmelting(@NotNull final TileState machineState) {
        final PersistentDataContainer machineData = machineState.getPersistentDataContainer();

        return Boolean.TRUE.equals(machineData.get(AUTOSMELTER_KEY, PersistentDataType.BOOLEAN));
    }

    public void takeAutoSmeltingUse(@NotNull final TileState machineState, final int usesToTake) {
        final int availableSmeltingUses = getAutoSmeltingUsesLeft(machineState);

        if (availableSmeltingUses < usesToTake)
            throw new IllegalArgumentException("Not enough uses left!");

        final PersistentDataContainer machineData = machineState.getPersistentDataContainer();

        final int newSmeltingUses = availableSmeltingUses - usesToTake;
        machineData.set(AUTOSMELTER_KEY, PersistentDataType.INTEGER, newSmeltingUses);

        machineState.update();
    }

    public boolean addAutoSmeltingUse(@NotNull final TileState machineState, final int usesToAdd) {
        final int availableSmeltingUses = getAutoSmeltingUsesLeft(machineState);

        if (availableSmeltingUses >= MAX_FUEL_CAPACITY)
            return false;

        final PersistentDataContainer machineData = machineState.getPersistentDataContainer();
        final int newSmeltingUses = availableSmeltingUses + usesToAdd;

        machineData.set(AUTOSMELTER_KEY, PersistentDataType.INTEGER, newSmeltingUses);

        return true;
    }

}
