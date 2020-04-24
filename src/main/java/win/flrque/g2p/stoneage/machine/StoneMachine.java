package win.flrque.g2p.stoneage.machine;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Dispenser;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.Directional;
import win.flrque.g2p.stoneage.StoneAge;

import java.util.ArrayList;
import java.util.List;

public class StoneMachine {

    public static final Material STONE_MACHINE_MATERIAL = Material.DISPENSER;

    private final StoneAge plugin;

    private final String machineName;
    private final List<String> machineLore = new ArrayList<>();

    private final ItemStack stoneMachineParent;
    private final BlockFace[] cubeDirections = new BlockFace[6];

    public StoneMachine(String machineName, List<String> lore) {
        this.plugin = StoneAge.getPlugin(StoneAge.class);

        this.machineName = ChatColor.translateAlternateColorCodes('&', machineName);

        for(String line : lore) {
            this.machineLore.add(ChatColor.translateAlternateColorCodes('&', line));
        }

        this.stoneMachineParent = createStoneMachineItem(STONE_MACHINE_MATERIAL);
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

    public boolean isConnectedToStoneMachine(Block block) {
        for(int i=0; i<6; i++) {
            final Block relativeBlock = block.getRelative(BlockFace.values()[i], 1);

            if(isStoneMachine(relativeBlock)) {
                final Dispenser stoneMachine = (Dispenser) relativeBlock.getState();
                final Directional direction = (Directional) stoneMachine.getData();

                if(direction.getFacing().getOppositeFace().equals(BlockFace.values()[i])) {
                    return true;
                }
            }
        }

        return false;
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

    public static List<String> createDefaultMachineLore() {
        final List<String> machineLore = new ArrayList<>();

        machineLore.add(ChatColor.GRAY + "Postaw stoniarke w kierunku,");
        machineLore.add(ChatColor.GRAY + "gdzie ma generowac stone!");
        machineLore.add(" ");
        machineLore.add(ChatColor.DARK_RED + "Uwaga! " + ChatColor.YELLOW + "Stoniarki mozna niszczyc");
        machineLore.add("tylko zlotym kilofem.");

        return machineLore;
    }

}