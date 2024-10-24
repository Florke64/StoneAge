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

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Dispenser;
import org.bukkit.block.data.Directional;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.florke.stoneage.StoneAge;
import pl.florke.stoneage.util.Message;

import java.util.*;
import java.util.logging.Level;

/**
 * This class defines how Stone Machines work and behave. It contains
 * configuration variables and methods to distinguish between Stone Machines
 * and regular Dispenser blocks.
 */
public class StoneMachine {

    public static final Material STONE_MACHINE_MATERIAL = Material.DISPENSER;
    public static final Material MACHINE_LABEL_MATERIAL = Material.PAPER;

    public static final int MACHINE_LABEL_SLOT = 0;

    private final StoneAge plugin;

    private final ItemAutoSmelter itemSmelter;

    private final String machineName;
    private final List<String> machineLore;

    private final Map<Dispenser, Long> lastStoneMachineRepair = new HashMap<>();
    private final ItemStack stoneMachineParent;
    private final ItemStack machineLabel;
    private long stoneRespawnFrequency = 40L;
    private int repairCooldown = 5;
    private boolean allowHopperOutput = false;
    private boolean allowHopperInput = false;
    private boolean dropItemsToFeet = false;
    private boolean dropExpToFeet = false;

    /**
     * @see StoneMachine
     */
    public StoneMachine(final String machineName, final List<String> machineLore) {
        this.plugin = StoneAge.getPlugin(StoneAge.class);

        this.itemSmelter = new ItemAutoSmelter();

        this.machineName = Message.color(machineName);

        final Message lore = new Message(machineLore);
        this.machineLore = lore.getCachedCompiledMessage();

        this.stoneMachineParent = createStoneMachineItem(STONE_MACHINE_MATERIAL);

        this.machineLabel = new ItemStack(MACHINE_LABEL_MATERIAL, 1);
        final ItemMeta im = this.machineLabel.getItemMeta();

        assert im != null;
        im.setLore(this.machineLore);
        im.setDisplayName(this.machineName);

        this.machineLabel.setItemMeta(im);
    }

    /**
     * @return {@link ArrayList} of a colored Stone Machine's default lore lines.
     */
    @NotNull
    public static List<String> createDefaultMachineLore() {
        final Message defaultMachineLore = new Message();

        return defaultMachineLore.getCachedCompiledMessage();
    }

    /**
     * Places stone block in location relative to given machine parameter.<br />
     * Method used by the manual stone machine repair option in a stone machine's GUI.
     *
     * @param machine {@link Dispenser} instance of Stone Machine which should be repaired.
     * @return {@code true} if successfully added given machine to the async repair queue.
     * @see StoneMachine#generateStone(Location)
     */
    public boolean repairStoneMachine(@NotNull final Dispenser machine) {
        boolean result = true;
        final long repairCooldownLimit = (System.currentTimeMillis() - (1000L * getRepairCooldown()));
        if (lastStoneMachineRepair.containsKey(machine) && lastStoneMachineRepair.get(machine) >= repairCooldownLimit) {
            //Player is trying to repair stone machine too frequently
            result = false;
        } else {
            lastStoneMachineRepair.put(machine, System.currentTimeMillis());
            final Location stoneLocation = getGeneratedStoneLocation(machine);
            generateStone(stoneLocation);
        }

        return result;
    }

    /**
     * Gets {@link Location} where stone block should be generated for given parameter.
     *
     * @param stoneMachine {@link Dispenser} instance of a stone machine.
     * @return where stone block should be generated for a stone machine given in a parameter.
     */
    public Location getGeneratedStoneLocation(@NotNull final Dispenser stoneMachine) {
        Location result = null;
        if (isStoneMachine(stoneMachine.getInventory())) {
            final Directional machine = (Directional) stoneMachine.getBlockData();
            result = stoneMachine.getBlock().getRelative(machine.getFacing()).getLocation();
        }

        return result;
    }

    public boolean isStoneMachine(@Nullable final Block block) {
        if (block == null || !(block.getState() instanceof Dispenser machine)) {
            return false;
        }

        if (machine.getCustomName() == null) {
            return false;
        }

        return machine.getCustomName().equalsIgnoreCase(this.machineName);
    }

    public boolean isStoneMachine(@Nullable final Inventory inventory) {
        if (inventory == null || inventory.getLocation() == null) {
            return false;
        }

        final Block block = inventory.getLocation().getBlock();

        return isStoneMachine(block);
    }

    public boolean isStoneMachine(@NotNull final Dispenser machine) {
        if (machine.getCustomName() == null) {
            return false;
        }

        return machine.getCustomName().equalsIgnoreCase(this.machineName);
    }

    @Nullable
    public Block getConnectedStoneMachine(@NotNull final Block block) {
        for (int i = 0; i < 6; i++) {
            final Block relativeBlock = block.getRelative(BlockFace.values()[i], 1);

            if (isStoneMachine(relativeBlock)) {
                final Directional machine = (Directional) relativeBlock.getBlockData();

                if (machine.getFacing().getOppositeFace().equals(BlockFace.values()[i])) {
                    return relativeBlock;
                }
            }
        }

        return null;
    }

    /**
     * Checks if block given in a parameter is connected to the stone machine.
     *
     * @param block to be verified.
     * @return 'true' if is connected with Stone Machine.
     * @see StoneMachine#getConnectedStoneMachine(Block)
     */
    public boolean isConnectedToStoneMachine(@NotNull final Block block) {
        //TODO: Check block type and return false if it is not a Material.STONE
        return getConnectedStoneMachine(block) != null;
    }

    /**
     * Generates stone block at given position with current stone respawn delay.
     *
     * @param location where stone block will be placed.
     * @see StoneMachine#generateStone(Location, long)
     * @see StoneMachine#setStoneRespawnFrequency(long)
     */
    public void generateStone(@NotNull final Location location) {
        generateStone(location, stoneRespawnFrequency);
    }

    /**
     * This method will wait given delay in async task, switch to the sync task
     * and then check if given location is connected to any Stone Machine block.
     * Will also verify if given location is "empty" (meaning, no blocks were placed in mean time).<br />
     * If everything is fine, then block type at given position is set to {@link Material#STONE}.
     *
     * @param location where stone block will be placed.
     * @param delay    value in server ticks to wait and place block.
     */
    public void generateStone(@NotNull final Location location, final long delay) {
        final Block block = Objects.requireNonNull(location.getWorld()).getBlockAt(location);

        new BukkitRunnable() {
            @Override
            public void run() {

                //Returning to the Main thread
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (!isConnectedToStoneMachine(block))
                            return;

                        if (!block.getType().equals(Material.AIR))
                            return;

                        block.setType(Material.STONE);
                    }
                }.runTask(plugin);
            }
        }.runTaskLaterAsynchronously(plugin, delay);
    }

    /**
     * Clones parent instance of Stone Machine item.
     *
     * @return {@link ItemStack} representing Stone Machine's item.
     * @see StoneMachine#createStoneMachineItem(Material)
     */
    public ItemStack createStoneMachineItem() {
        return stoneMachineParent.clone();
    }

    /**
     * Creates new Stone-Machine-alike {@link ItemStack}.
     *
     * @param material Currently, only {@link Material#DISPENSER} is acceptable.
     * @return {@link ItemStack} representing Stone Machine's item.
     */
    @NotNull
    private ItemStack createStoneMachineItem(@NotNull final Material material) {
        final ItemStack item = new ItemStack(material);
        final ItemMeta meta = item.getItemMeta();

        //TODO: Add appropriate configuration sections (issue #18)
        assert meta != null;
        meta.setDisplayName(machineName);
        meta.setLore(machineLore);

        item.setItemMeta(meta);

        return item;
    }

    /**
     * @return {@link String} of a current effective Stone Machine item's display name.
     */
    @SuppressWarnings("unused")
    public String getMachineName() {
        return machineName;
    }

    /**
     * @return {@link ArrayList} of a current effective Stone Machine item's lore.
     * @see StoneMachine#createDefaultMachineLore()
     */
    @SuppressWarnings("unused")
    public List<String> getMachineLore() {
        if (machineLore == null || machineLore.isEmpty()) {
            createDefaultMachineLore();
        }

        return machineLore;
    }

    /**
     * Creates Stone Machine's crafting recipe and registers it under the unique {@link NamespacedKey}.<br />
     * It won't register crafting recipe if {@link NamespacedKey} is already in use.
     */
    public void registerCraftingRecipe() {
        final NamespacedKey namespacedKey = new NamespacedKey(this.plugin, "stone_machine");

        final Iterator<Recipe> registeredRecipes = Bukkit.recipeIterator();
        while (registeredRecipes.hasNext()) {
            final Recipe recipe = registeredRecipes.next();
            if (!(recipe instanceof ShapedRecipe)) continue;

            if (((ShapedRecipe) recipe).getKey().getKey().contentEquals(namespacedKey.getKey())) {
                new Message("Skipping crafting recipe registration, as the same NamespaceKey was already reserved.")
                        .log(Level.WARNING);
                return;
            }
        }

        final ShapedRecipe craftingRecipe = new ShapedRecipe(namespacedKey, this.stoneMachineParent);
        craftingRecipe.shape("CLC", "RDR", "CPC");

        craftingRecipe.setIngredient('C', Material.COAL_BLOCK);
        craftingRecipe.setIngredient('L', Material.LAVA_BUCKET);
        craftingRecipe.setIngredient('R', Material.REDSTONE);
        craftingRecipe.setIngredient('D', StoneMachine.STONE_MACHINE_MATERIAL);
        craftingRecipe.setIngredient('P', Material.PISTON);

        Bukkit.addRecipe(craftingRecipe);
    }

    /**
     * @see ItemAutoSmelter
     */
    public ItemAutoSmelter getItemSmelter() {
        return itemSmelter;
    }

    /**
     * Machine Label is stored in Stone Machines.
     * It is a dummy ItemStack, it represents that Dispenser which contains it is a Stone Machine.
     *
     * @return sample {@link ItemStack} of this kind.
     */
    public ItemStack getMachineLabel() {
        return machineLabel;
    }

    /**
     * 'machines.stone_frequency' in the config.yml file.
     *
     * @param stoneRespawnFrequency time in which stone blocks will regenerate after breaking (in server ticks).
     */
    public void setStoneRespawnFrequency(final long stoneRespawnFrequency) {
        this.stoneRespawnFrequency = stoneRespawnFrequency;
    }

    /**
     * 'machines.repair_cooldown' in the config.yml file.
     *
     * @return stone machine's manual repair cooldown in seconds.
     */
    public int getRepairCooldown() {
        return repairCooldown;
    }

    /**
     * 'machines.repair_cooldown' in the config.yml file.
     *
     * @param repairCooldown cooldown for manual machine repair (in seconds).
     */
    public void setRepairCooldown(final int repairCooldown) {
        this.repairCooldown = repairCooldown;
    }

    /**
     * 'machines.allow_hopper_output' in the config.yml file.
     *
     * @return 'true' if stone machine is allowed to put dropped items into the hopper under it.
     */
    public boolean isHopperOutputAllowed() {
        return allowHopperOutput;
    }

    /**
     * 'machines.allow_hopper_output' in the config.yml file.
     *
     * @param allow 'true' allows stone machines to put dropped items into the hopper under them instead of dropping them at the player.
     */
    public void setAllowHopperOutput(final boolean allow) {
        this.allowHopperOutput = allow;
    }

    /**
     * 'machines.allow_hopper_input' in the config.yml file.
     *
     * @return 'true' if stone machine is allowed to get auto-smelting fuel from attached hoppers.
     */
    public boolean isHopperInputAllowed() {
        return allowHopperInput;
    }

    /**
     * 'machines.allow_hopper_input' in the config.yml file.
     *
     * @param allow 'true' allows stone machines to get auto-smelting fuel from attached hoppers.
     */
    public void setAllowHopperInput(final boolean allow) {
        this.allowHopperInput = allow;
    }

    /**
     * 'machines.drop_items_to_feet' in the config.yml file.
     *
     * @return 'true' if stone machine drops are dropped under player's feet instead of being dropped at destroyed stone blocks location.
     */
    public boolean isDropItemsToFeet() {
        return dropItemsToFeet;
    }

    /**
     * 'machines.drop_items_to_feet' in the config.yml file.
     *
     * @param dropToFeet 'true' makes stone machines to drop items under player's feet instead of dropping where stone block was destroyed.
     */
    public void setDropItemsToFeet(final boolean dropToFeet) {
        this.dropItemsToFeet = dropToFeet;
    }

    /**
     * 'machines.drop_exp_to_feet' in the config.yml file.
     *
     * @return 'true' if exp orbs are spawned under player's feet instead of being spawned where stone block was destroyed.
     */
    public boolean isDropExpToFeet() {
        return dropExpToFeet;
    }

    /**
     * 'machines.drop_exp_to_feet' in the config.yml file.
     *
     * @param dropExpToFeet 'true' makes stone machines to spawn exp orbs under players feet.
     */
    public void setDropExpToFeet(boolean dropExpToFeet) {
        this.dropExpToFeet = dropExpToFeet;
    }

}
