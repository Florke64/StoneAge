/*
 * Copyright Go2Play.pl (c) 2020.
 * Program made for Go2Play Skyblock server. It's not allowed to re-distribute the code.
 * Author: FlrQue
 */

package win.flrque.g2p.stoneage.util;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import win.flrque.g2p.stoneage.drop.DropEntry;

import java.util.logging.Level;

public class ConfigSectionDropEntry extends ConfigSectionReader {

    public ConfigSectionDropEntry(ConfigurationSection section) {
        super(section);
    }

    public DropEntry compileDropEntry() {
        //Getting drop's chance weight
        final float weight = readDropChanceWeight();

        //Reading actual drops
        ItemStack defaultToolItem = null;
        ItemStack silkToolItem = null;

        final String canonicalEntryName = rootSection.getName();

        //Reading drops for default tool
        final ConfigurationSection defaultToolSection = rootSection.getConfigurationSection("default_tool");
        if(defaultToolSection != null) {
            final ConfigSectionItemStack defaultToolConfig = new ConfigSectionItemStack(defaultToolSection);

            defaultToolItem = defaultToolConfig.getItemStack();
        }

        //Reading drops for tools with Silk Touch enchantment
        final ConfigurationSection silkToolSection = rootSection.getConfigurationSection("silk_touch_tool");
        if(silkToolSection != null) {
            final ConfigSectionItemStack silkToolConfig = new ConfigSectionItemStack(silkToolSection);

            silkToolItem = silkToolConfig.getItemStack();
        }

        //If there was an error while reading those sections
        if(defaultToolItem == null && silkToolItem == null) {
            plugin.getLogger().log(Level.SEVERE, "Error while reading DropEntry: \""+ rootSection.getName() +"\".");
            return null;
        }

        defaultToolItem = (defaultToolItem != null)? defaultToolItem : silkToolItem;
        final DropEntry dropEntry = new DropEntry(canonicalEntryName, defaultToolItem, weight);

        //Setting Silk Touch enchantment drop
        if(silkToolItem != null)
            dropEntry.setSilkTouchItemStack(silkToolItem);

        //Fortune Enchant ignoring
        final String customEntryName = rootSection.getString("custom_name", canonicalEntryName);
        dropEntry.setCustomName(customEntryName);

        //Fortune Enchant ignoring
        final boolean ignoreFortune = rootSection.getBoolean("ignore_fortune", true);
        dropEntry.setIgnoreFortuneEnchant(ignoreFortune);

        //Accepts drop multiplication set by server admin
        boolean multipliable = rootSection.getBoolean("multipliable", true);
        dropEntry.setMultipliable(multipliable);

        //Minimal and Maximal drop count
        final int minAmount = defaultToolSection.getInt("minimal_amount", -1);
        final int maxAmount = defaultToolSection.getInt("maximal_amount", -1);
        dropEntry.setMinAmount(minAmount);
        dropEntry.setMaxAmount(maxAmount);

        //Experience Drops
        final int minExp = rootSection.getInt("minimal_exp", -1);
        final int maxExp = rootSection.getInt("maximal_exp", -1);
        dropEntry.setMinimalExp(minExp);
        dropEntry.setMaximalExp(maxExp);

        return dropEntry;
    }

    private float readDropChanceWeight() {
        final String weightString = rootSection.getString("weight");

        float weight;
        try {
            weight = Float.parseFloat(weightString);
        } catch (NumberFormatException ex) {
            weight = 0.0f;
            ex.printStackTrace();
        }

        return weight;
    }

    private ItemStack readItemStackDrop() {
        return null;
    }

}
