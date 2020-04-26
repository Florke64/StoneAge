package win.flrque.g2p.stoneage.util;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import win.flrque.g2p.stoneage.StoneAge;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class ConfigSectionItemStack {

    private final StoneAge plugin;
    private final ConfigurationSection rootSection;

    public ConfigSectionItemStack(ConfigurationSection section) {
        plugin = StoneAge.getPlugin(StoneAge.class);

        rootSection = section;
    }

    @SuppressWarnings("deprecation")
    public ItemStack compileItemStack() throws InvalidConfigurationException {
        //Getting material type from config section
        final Material itemMaterial = readMaterial();
        if(itemMaterial == null)
            throw new InvalidConfigurationException("Invalid Material name. Please refer to Bukkit's Material enum class.");

        final ItemStack itemStack = new ItemStack(itemMaterial);

        //Getting data magic value
        final byte magicValue = (byte) readMagicValue();
        if(magicValue < 0)
            plugin.getLogger().log(Level.WARNING, "Magic value for " + itemMaterial + " wasn't specified correctly. Using default value...");

        itemStack.getData().setData(magicValue < 0? 0 : magicValue);

        final ItemMeta itemMeta = itemStack.getItemMeta();

        //Getting item's custom name
        final String customName = readCustomName();
        if(!customName.equals(""))
            itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', customName));

        //Reading item's lore
        final List<String> lore = readLore();
        if(lore != null && !lore.isEmpty())
            itemMeta.setLore(lore);

        //Reading enchantments
        final Map<Enchantment, Integer> enchants = readEnchantments();
        for(Enchantment enchantment : enchants.keySet()) {
            itemMeta.addEnchant(enchantment, enchants.get(enchantment), true);
        }

        //Warping-ip the ItemStack
        itemStack.setItemMeta(itemMeta);

        return itemStack;
    }

    private Material readMaterial() {
        final String materialName = rootSection.getString("material");
        return Material.getMaterial(materialName.toUpperCase());
    }

    private int readMagicValue() {
        return rootSection.getInt("magic_data", -1);
    }

    private String readCustomName() {
        return rootSection.getString("custom_name", "");
    }

    private List<String> readLore() {
        final List<String> lore = new ArrayList<>();
        for(String line : rootSection.getStringList("lore")) {
            lore.add(ChatColor.translateAlternateColorCodes('&', line));
        }

        return lore;
    }

    private Map<Enchantment, Integer> readEnchantments() {
        final List<String> enchantmentList = rootSection.getStringList("enchantments");
        final Map<Enchantment, Integer> enchantments = new HashMap<>();

        for(String ench : enchantmentList) {
            final String[] enchantmentData = ench.split(" ", 2);

            final Enchantment enchantment;
            final int enchantmentLevel;

            enchantment = Enchantment.getByName(enchantmentData[0].toUpperCase());
            enchantmentLevel = (enchantmentData.length<2)? 1 : Integer.parseInt(enchantmentData[1]);

            if(enchantment != null) {
                enchantments.put(enchantment, enchantmentLevel);
            } else {
                plugin.getLogger().log(Level.WARNING, "Invalid Enchantment for the item found - please double check the config.yml file!");
            }
        }

        return enchantments;
    }
}
