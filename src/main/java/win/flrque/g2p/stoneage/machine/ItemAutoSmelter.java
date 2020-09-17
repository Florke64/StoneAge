/*
 * Copyright Go2Play.pl (c) 2020.
 * Program made for Go2Play Skyblock server. It's not allowed to re-distribute the code.
 * Author: FlrQue
 */

package win.flrque.g2p.stoneage.machine;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Dispenser;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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
        if (!plugin.getStoneMachine().isStoneMachine(stoneMachine))
            return -1;

        if (!hasAutoSmelting(stoneMachine)) {
            return 0;
        }

        final ItemStack magicCoal = getMagicCoal(stoneMachine);

        // Asserting that Magic Coal and it's data ain't null as #hasAutoSmelting(...) returned true
        assert magicCoal != null && magicCoal.getItemMeta() != null;
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

        if (!hasAutoSmelting(stoneMachine))
            return;

        final int availableSmeltingUses = getAutoSmeltingUsesLeft(stoneMachine);

        // Asserting that Magic Coal and it's data ain't null as #hasAutoSmelting(...) returned true
        assert magicCoal != null && magicCoal.getItemMeta() != null;
        final ItemMeta coalIm = magicCoal.getItemMeta();

        coalIm.setDisplayName(Integer.toString(availableSmeltingUses - usesToTake));
        magicCoal.setItemMeta(coalIm);
    }

    public boolean addAutoSmeltingUse(@Nullable final Inventory stoneMachineInventory, final int usesToAdd) {
        if (stoneMachineInventory == null || stoneMachineInventory.getLocation() == null) {
            return false;
        }

        final Block block = stoneMachineInventory.getLocation().getBlock();

        return addAutoSmeltingUse(block, usesToAdd);
    }

    public boolean addAutoSmeltingUse(@NotNull final Block stoneMachineBlock, final int usesToAdd) {
        if(!(stoneMachineBlock.getState() instanceof Dispenser)) {
            return false;
        }

        final Dispenser dispenserStoneMachine = (Dispenser) stoneMachineBlock.getState();
        final ItemStack magicCoal = getMagicCoal(dispenserStoneMachine.getInventory());

        if (magicCoal == null) {
            return initMagicCoalForMachine(dispenserStoneMachine.getInventory(), usesToAdd);
        }

        final int availableSmeltingUses = getAutoSmeltingUsesLeft(dispenserStoneMachine);

        if (availableSmeltingUses >= MAX_FUEL_CAPACITY)
            return false;

        final ItemMeta magicCoalItemMeta = magicCoal.getItemMeta();

        /* Asserting that magicCoalItemMeta isn't null because #getMagicCoal(...) returned not null value
         *  which means that #hasAutoSmelting(...) will also return true */
        assert magicCoalItemMeta != null;

        magicCoalItemMeta.setDisplayName(Integer.toString(availableSmeltingUses + usesToAdd));
        magicCoal.setItemMeta(magicCoalItemMeta);

        return true;
    }

    private boolean hasAutoSmelting(@NotNull final InventoryHolder inventoryHolder) {
        if (!(inventoryHolder instanceof Dispenser)) {
            return false;
        }

        return getMagicCoal(inventoryHolder) != null;
    }

    private boolean hasAutoSmelting(@NotNull final Inventory machineInventory) {
        return getMagicCoal(machineInventory) != null;
    }

    private @Nullable ItemStack getMagicCoal(@Nullable final InventoryHolder inventoryHolder) {
        if (!(inventoryHolder instanceof Dispenser)) {
            return null;
        }

        return getMagicCoal(inventoryHolder.getInventory());
    }

    private @Nullable ItemStack getMagicCoal(@NotNull final Inventory machineInventory) {
        final ItemStack magicCoal = machineInventory.getItem(ItemAutoSmelter.MAGIC_COAL_SLOT);

        // Auto Smelting was never initialized for this stone machine
        if (magicCoal == null || magicCoal.getType() != Material.COAL) {
            return null;
        }

        // Some weird bug, this is not a real "magic coal"
        final ItemMeta coalMeta = magicCoal.getItemMeta();
        if (coalMeta == null || !magicCoal.getItemMeta().hasDisplayName()) {
            return null;
        }

        return magicCoal;
    }

    private boolean initMagicCoalForMachine(@NotNull final Inventory machineInventory, final int startingUses) {
        if (hasAutoSmelting(machineInventory))
            return false;

        final ItemStack magicCoal = new ItemStack(Material.COAL, 1);
        final ItemMeta im = magicCoal.getItemMeta();

        // Asserting that ItemMeta isn't null as it's newly created ItemStack
        assert im != null;

        // Setting the Display Name of Magic Coal
        im.setDisplayName(Integer.toString(startingUses));
        magicCoal.setItemMeta(im);

        // Placing an ItemStack in the slot
        machineInventory.setItem(ItemAutoSmelter.MAGIC_COAL_SLOT, magicCoal);

        return true;
    }

}
