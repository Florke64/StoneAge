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

import net.kyori.adventure.text.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.TileState;
import org.bukkit.block.data.Directional;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.florke.stoneage.StoneAge;
import pl.florke.stoneage.config.GeneralConfigReader;
import pl.florke.stoneage.util.Message;

import java.util.*;
import java.util.logging.Level;

/**
 * This class defines how Stone Machines work and behave. It contains
 * configuration variables and methods to distinguish between Stone Machines
 * and regular Dispenser blocks.
 */
public class StoneMachine {

    public static final String STONE_MACHINE_IDENTIFIER_NAME = "stone_machine";

    private final StoneAge plugin;
    private final NamespacedKey machineIdentifierKey;

    public static final Material STONE_MACHINE_MATERIAL = Material.DISPENSER;

    private final ItemAutoSmelter itemSmelter;

    private final TextComponent machineName;
    private final List<TextComponent> machineLore;

    // This item is cloned, it represents a machine
    private final ItemStack stoneMachineParent;

    private final Map<TileState, Long> lastStoneMachineRepair = new HashMap<>();
    private ItemStack machineDestroyTool = new ItemStack(Material.GOLDEN_PICKAXE);
    private long stoneRespawnFrequency = 40L;
    private int repairCooldown = 5;
    private boolean allowHopperOutput = false;
    private boolean allowHopperInput = false;
    private boolean dropItemsToFeet = false;
    private boolean dropExpToFeet = false;

    /**
     * @see StoneMachine
     */
    public StoneMachine() {
        this.plugin = StoneAge.getPlugin(StoneAge.class);
        this.machineIdentifierKey = new NamespacedKey(this.plugin, STONE_MACHINE_IDENTIFIER_NAME);

        this.machineName = Message.color(plugin.getLanguage("stone-machine-item-name"));

        this.machineLore = new Message().asComponents();

        ItemStack _stoneMachineParent;
        try {
            _stoneMachineParent = createStoneMachineItem(STONE_MACHINE_MATERIAL);
        } catch (IllegalArgumentException ex) {
            _stoneMachineParent = new ItemStack(STONE_MACHINE_MATERIAL);
        }

        this.stoneMachineParent = _stoneMachineParent;

        // machine extension
        this.itemSmelter = new ItemAutoSmelter();
    }

    /**
     * Places stone block in location relative to given machine parameter.<br />
     * Method used by the manual stone machine repair option in a stone machine's GUI.
     *
     * @param machine {@link TileState} instance of Stone Machine which should be repaired.
     * @return {@code true} if successfully added given machine to the async repair queue.
     * @see StoneMachine#generateStone(Location)
     */
    public boolean repairStoneMachine(@NotNull final TileState machine) {
        boolean result = true;

        final long repairCooldownLimit = (System.currentTimeMillis() - (1000L * getRepairCooldown()));
        if (lastStoneMachineRepair.containsKey(machine) && lastStoneMachineRepair.get(machine) >= repairCooldownLimit) {
            //Player is trying to repair stone machine too frequently
            result = false;
        } else {
            final Location stoneLocation = getGeneratedStoneLocation(machine);
            generateStone(stoneLocation);

            lastStoneMachineRepair.put(machine, System.currentTimeMillis());
        }

        return result;
    }

    /**
     * Gets {@link Location} where stone block should be generated for given parameter.
     *
     * @param stoneMachine {@link TileState} instance of a stone machine.
     * @return where stone block should be generated for a stone machine given in a parameter.
     */
    public Location getGeneratedStoneLocation(@NotNull final TileState stoneMachine) {
        if (!isStoneMachine(stoneMachine))
            return null;

        final Directional machine = (Directional) stoneMachine.getBlockData();
        return stoneMachine.getBlock().getRelative(machine.getFacing()).getLocation();
    }

    public boolean isStoneMachine(@NotNull final TileState machineState) {
        final PersistentDataContainer machineData = machineState.getPersistentDataContainer();
        final Boolean isStoneMachine = machineData.get(machineIdentifierKey, PersistentDataType.BOOLEAN);

        return Boolean.TRUE.equals(isStoneMachine);
    }

    public boolean isStoneMachine(@NotNull final ItemStack machineItem) {
        if (!machineItem.hasItemMeta())
            return false;

        final ItemMeta meta = machineItem.getItemMeta();
        final PersistentDataContainer machineData = meta.getPersistentDataContainer();
        final Boolean isStoneMachine = machineData.get(machineIdentifierKey, PersistentDataType.BOOLEAN);

        return Boolean.TRUE.equals(isStoneMachine);
    }

    @Nullable
    public Block getConnectedStoneMachine(@NotNull final Block block) {
        for (int i = 0; i < 6; i++) {
            final Block relativeBlock = block.getRelative(BlockFace.values()[i], 1);

            if (!(relativeBlock.getState() instanceof TileState machineState))
                continue;

            if (isStoneMachine(machineState)) {
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
    private ItemStack createStoneMachineItem(final Material material) {
        final ItemStack machineItem;

        machineItem = new ItemStack(
            (material != null && material.equals(STONE_MACHINE_MATERIAL))?
                material : Material.DISPENSER);

        final ItemMeta meta = machineItem.getItemMeta();
        final PersistentDataContainer persistentDataContainer = meta.getPersistentDataContainer();
        persistentDataContainer.set(machineIdentifierKey, PersistentDataType.BOOLEAN, true);

        meta.displayName(Message.color(machineName));
        machineItem.setItemMeta(meta);

        return machineItem;
    }

    /**
     * @return {@link String} of a current effective Stone Machine item's display name.
     */
    @SuppressWarnings("unused")
    public TextComponent getMachineName() {
        return machineName;
    }

    /**
     * @return {@link ArrayList} of a current effective Stone Machine item's lore.
     */
    @SuppressWarnings("unused")
    public List<TextComponent> getMachineLore() {
        if (this.machineLore == null)
            return new Message().asComponents();

        return List.copyOf(machineLore);
    }

    /**
     * Creates Stone Machine's crafting recipe and registers it under the unique {@link NamespacedKey}.<br />
     * It won't register crafting recipe if {@link NamespacedKey} is already in use.
     */
    public void registerCraftingRecipe() {
        final NamespacedKey namespacedKey = new NamespacedKey(this.plugin, STONE_MACHINE_IDENTIFIER_NAME);

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
     * Retrieves the {@link NamespacedKey} used to identify Stone Machine in the PersistenceDataContainer.<br />
     * This key is used to determine whether a certain block is a Stone Machine or not.
     *
     * @return the unique NamespaceKey used to identify Stone Machine.
     */
    public NamespacedKey getMachineIdentifierKey() {
        return machineIdentifierKey;
    }

    public void applyMachineConfiguration(GeneralConfigReader generalConfig) {
        setStoneRespawnFrequency(generalConfig.getStoneFrequency());
        setRepairCooldown(generalConfig.getRepairCoolDown());
        setDropItemsToFeet(generalConfig.isDropItemsToFeet());
        setDropExpToFeet(generalConfig.isDropExpToFeet());
        setAllowHopperOutput(generalConfig.isAllowHopperDropOutput());
        setAllowHopperInput(generalConfig.isAllowCoalUpgradesByHopper());
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

    public boolean isMachineDestroyTool(final ItemStack tool) {
        return machineDestroyTool.isSimilar(tool.clone());
    }

    public void setMachineDestroyTool(final ItemStack tool) {
        if (tool == null)
            throw new IllegalArgumentException("Wrong machine destroy tool provided");
        machineDestroyTool = tool.clone();
    }

}
