/*
 * Copyright Go2Play.pl (c) 2020.
 * Program made for Go2Play Skyblock server. It's not allowed to re-distribute the code.
 * Author: FlrQue
 */

package win.flrque.g2p.stoneage.machine;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Dispenser;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.Directional;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import win.flrque.g2p.stoneage.StoneAge;
import win.flrque.g2p.stoneage.util.Message;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StoneMachine {

    public static final Material STONE_MACHINE_MATERIAL = Material.DISPENSER;

    private final StoneAge plugin;

    private final String machineName;
    private final List<String> machineLore;

    private final Map<Dispenser, Long> lastStoneMachineRepair = new HashMap<>();

    private long stoneRespawnFrequency = 40l;
    private int repairCooldown = 5;

    private boolean allowHopperOutput = false;

    private boolean dropItemsToFeet = false;
    private boolean dropExpToFeet = false;

    private final ItemStack stoneMachineParent;
    private final ItemStack machineLabel;

    public StoneMachine(String machineName, List<String> machineLore) {
        this.plugin = StoneAge.getPlugin(StoneAge.class);

        this.machineName = Message.color(machineName);

        final Message lore = new Message(machineLore);
        this.machineLore = lore.getPreparedMessage();
        
        this.stoneMachineParent = createStoneMachineItem(STONE_MACHINE_MATERIAL);

        this.machineLabel = new ItemStack(Material.PAPER, 1);
        final ItemMeta im = this.machineLabel.getItemMeta();
        im.setLore(this.machineLore);
        im.setDisplayName(this.machineName);

        this.machineLabel.setItemMeta(im);
    }

    public boolean repairStoneMachine(Dispenser machine) {
        final long repairCooldownLimit = (System.currentTimeMillis() - (1000 * repairCooldown));
        if(lastStoneMachineRepair.containsKey(machine) && lastStoneMachineRepair.get(machine) >= repairCooldownLimit ) {
            //Player is trying to repair stone machine too frequently
            return false;
        }

        lastStoneMachineRepair.put(machine, System.currentTimeMillis());
        final Location stoneLocation = getGeneratedStoneLocation(machine);

        generateStone(stoneLocation);

        return true;
    }

    public boolean isStoneMachine(Block block) {
        if(block.getState() instanceof Dispenser) {
            return isStoneMachine((Dispenser) block.getState());
        }
        
        return false;
    }

    public boolean isStoneMachine(Dispenser dispenserBlock) {
        if(dispenserBlock.getCustomName() == null)
            return false;

        return dispenserBlock.getCustomName().equals(this.machineName);
    }

    public boolean isStoneMachine(Inventory inventory) {
        if(inventory == null) {
            return false;
        }

        return inventory.contains(this.machineLabel);
    }

    public Location getGeneratedStoneLocation(Dispenser stoneMachine) {
        if(!isStoneMachine(stoneMachine))
            return null;

        final Directional machine = (Directional) stoneMachine.getData();

        return stoneMachine.getBlock().getRelative(machine.getFacing()).getLocation();
    }

    public Block getConnectedStoneMachine(Block block) {
        for(int i=0; i<6; i++) {
            final Block relativeBlock = block.getRelative(BlockFace.values()[i], 1);

            if(isStoneMachine(relativeBlock)) {
                final Dispenser stoneMachine = (Dispenser) relativeBlock.getState();
                final Directional direction = (Directional) stoneMachine.getData();

                if(direction.getFacing().getOppositeFace().equals(BlockFace.values()[i])) {
                    return relativeBlock;
                }
            }
        }

        return null;
    }

    public boolean isConnectedToStoneMachine(Block block) {
        return getConnectedStoneMachine(block) != null;
    }

    public void generateStone(final Location location) {
        generateStone(location, stoneRespawnFrequency);
    }

    public void generateStone(final Location location, final long delay) {
        final Block block = location.getWorld().getBlockAt(location);

        new BukkitRunnable() {
            @Override
            public void run() {

                //Returning to the Main thread
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if(!isConnectedToStoneMachine(block))
                            return;

                        if(!block.getType().equals(Material.AIR))
                            return;

                        block.setType(Material.STONE);
                    }
                }.runTask(plugin);
            }
        }.runTaskLaterAsynchronously(plugin, delay);
    }

    public ItemStack createStoneMachineItem() {
        return stoneMachineParent.clone();
    }

    private ItemStack createStoneMachineItem(Material material) {
        final ItemStack item = new ItemStack(material);
        final ItemMeta meta = item.getItemMeta();

        //TODO: Add appropriate configuration sections
        meta.setDisplayName(machineName);
        meta.setLore(machineLore);

        item.setItemMeta(meta);

        return item;
    }

    public ItemStack getExample() {
        return stoneMachineParent;
    }

    public String getMachineName() {
        return machineName;
    }

    public List<String> getMachineLore() {
        if(machineLore == null || machineLore.isEmpty()) {
            createDefaultMachineLore();
        }

        return machineLore;
    }

    @NotNull
    public static List<String> createDefaultMachineLore() {
        final List<String> machineLore = new ArrayList<>();

        machineLore.add(ChatColor.GRAY + "Postaw stoniarke w kierunku,");
        machineLore.add(ChatColor.GRAY + "gdzie ma generowac stone!");
        machineLore.add(" ");
        machineLore.add(ChatColor.DARK_RED + "Uwaga! " + ChatColor.YELLOW + "Stoniarki mozna niszczyc");
        machineLore.add(ChatColor.YELLOW + "tylko zlotym kilofem.");

        return machineLore;
    }

    public void setStoneRespawnFrequency(long stoneRespawnFrequency) {
        this.stoneRespawnFrequency = stoneRespawnFrequency;
    }

    public boolean isHopperOutputAllowed() {
        return allowHopperOutput;
    }

    public void setAllowHopperOutput(boolean allow) {
        this.allowHopperOutput = allow;
    }

    public boolean isDropItemsToFeet() {
        return dropItemsToFeet;
    }

    public void setDropItemsToFeet(boolean dropItemsToFeet) {
        this.dropItemsToFeet = dropItemsToFeet;
    }

    public boolean isDropExpToFeet() {
        return dropExpToFeet;
    }

    public void setDropExpToFeet(boolean dropExpToFeet) {
        this.dropExpToFeet = dropExpToFeet;
    }

    public ItemStack getMachineLabel() {
        return machineLabel;
    }
}
