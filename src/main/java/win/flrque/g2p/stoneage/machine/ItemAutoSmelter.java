/*
 * Copyright Go2Play.pl (c) 2020.
 * Program made for Go2Play Skyblock server. It's not allowed to re-distribute the code.
 * Author: FlrQue
 */

package win.flrque.g2p.stoneage.machine;

import org.bukkit.Material;
import org.bukkit.block.Dispenser;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import win.flrque.g2p.stoneage.StoneAge;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

public class ItemAutoSmelter {

    public static final int MAGIC_COAL_SLOT = 1;

    // Equivalent of Math.pow(2, 24) = 2^24
    public static final int MAX_FUEL_CAPACITY = 1 << 24;

    private final StoneAge plugin;

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
            if (!(recipe instanceof FurnaceRecipe))
                continue;

            final FurnaceRecipe smeltingRecipe = (FurnaceRecipe) recipe;
            this.smeltingRecipeList.add(smeltingRecipe);
            index++;
        }

        plugin.getLogger().log(Level.INFO, "Cached " + index + " smelting recipes.");
    }

    public ItemStack getSmelted(@NotNull final Dispenser stoneMachine, @NotNull final ItemStack itemToSmelt) {
        for (final FurnaceRecipe recipe : this.smeltingRecipeList) {
            final ItemStack input = recipe.getInput();

            //Checking if input is similar to the item provided as argument
            if (input.getType() == itemToSmelt.getType()) {
                final ItemStack smeltedItemStack = recipe.getResult();
                smeltedItemStack.setAmount(itemToSmelt.getAmount());

                takeAutoSmeltingUse(stoneMachine, itemToSmelt.getAmount());

                return smeltedItemStack;
            }
        }

        return null;
    }

    public int getAutoSmeltingUsesLeft(@NotNull final Dispenser stoneMachine) {
        final Inventory machineInventory = stoneMachine.getInventory();
        final ItemStack magicCoal = machineInventory.getItem(ItemAutoSmelter.MAGIC_COAL_SLOT);

        if (!hasAutoSmelting(stoneMachine.getInventory()))
            return 0;

        final String coalCustomName = magicCoal.getItemMeta().getDisplayName();

        try {
            return Integer.parseInt(coalCustomName);
        } catch (final NumberFormatException ex) {
            ex.printStackTrace();
            return 0;
        }
    }

    public void takeAutoSmeltingUse(@NotNull final Dispenser stoneMachine, final int usesToTake) {
        final Inventory machineInventory = stoneMachine.getInventory();
        final ItemStack magicCoal = machineInventory.getItem(ItemAutoSmelter.MAGIC_COAL_SLOT);

        if (!hasAutoSmelting(stoneMachine.getInventory()))
            return;

        final int availableSmeltingUses;
        final String coalCustomName = magicCoal.getItemMeta().getDisplayName();
        try {
            availableSmeltingUses = Integer.parseInt(coalCustomName);
        } catch (final NumberFormatException ex) {
            ex.printStackTrace();
            return;
        }

        final ItemMeta coalIm = magicCoal.getItemMeta();
        coalIm.setDisplayName(Integer.toString(availableSmeltingUses - usesToTake));
        magicCoal.setItemMeta(coalIm);
    }

    public boolean addAutoSmeltingUse(@NotNull final Inventory machineInventory, final int usesToAdd) {
        if (!hasAutoSmelting(machineInventory)) {
            final ItemStack magicCoal = new ItemStack(Material.COAL, 1);
            final ItemMeta im = magicCoal.getItemMeta();
            im.setDisplayName(Integer.toString(usesToAdd));
            magicCoal.setItemMeta(im);

            machineInventory.setItem(ItemAutoSmelter.MAGIC_COAL_SLOT, magicCoal);
        } else {
            final ItemStack magicCoal = machineInventory.getItem(ItemAutoSmelter.MAGIC_COAL_SLOT);

            final int availableSmeltingUses;
            final String coalCustomName = magicCoal.getItemMeta().getDisplayName();
            try {
                availableSmeltingUses = Integer.parseInt(coalCustomName);
            } catch (final NumberFormatException ex) {
                ex.printStackTrace();
                return false;
            }

            if (availableSmeltingUses >= MAX_FUEL_CAPACITY)
                return false;

            final ItemMeta coalIm = magicCoal.getItemMeta();
            coalIm.setDisplayName(Integer.toString(availableSmeltingUses + usesToAdd));
            magicCoal.setItemMeta(coalIm);
        }

        return true;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean hasAutoSmelting(@NotNull final Inventory machineInventory) {
        final ItemStack magicCoal = machineInventory.getItem(ItemAutoSmelter.MAGIC_COAL_SLOT);

        //Auto Smelting was never initialized for this stone machine
        if (magicCoal == null || magicCoal.getType() != Material.COAL) {
            return false;
        }

        //Some weird bug, this is not a real "magic coal"
        final ItemMeta coalMeta = magicCoal.getItemMeta();
        return coalMeta != null && magicCoal.getItemMeta().hasDisplayName();
    }

}
