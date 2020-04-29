/*
 * Copyright Go2Play.pl (c) 2020.
 * Program made for Go2Play Skyblock server. It's not allowed to re-distribute the code.
 * Author: FlrQue
 */

package win.flrque.g2p.stoneage.util;

import org.bukkit.configuration.ConfigurationSection;

public class ConfigSectionGeneral extends ConfigSectionReader {

    //Drop multiplier settings
    private float defaultDropMultiplier, maxDropMultiplier;

    //Values in seconds
    private int commandsCoolDown, repairCoolDown;

    //Value in game ticks
    private long stoneFrequency;

    //Life-ease functionalities
    private boolean dropExpToFeet, dropItemsToFeet;

    //Stone Machines Behaviour
    private boolean multipleStoneDrops;
    private boolean alwaysDropPrimitiveItem;
    private boolean alwaysUseCustomDrops;

    //Redstone-like interactions with stone machine
    private boolean allowHopperDropOutput;
    private boolean allowCoalUpgradesByHopper;

    public ConfigSectionGeneral(ConfigurationSection section) {
        super(section);
    }

    public boolean compile() {
        defaultDropMultiplier = Float.parseFloat(rootSection.getString("default_drop_multiplier", "1.0"));
        maxDropMultiplier = Float.parseFloat(rootSection.getString("max_drop_multiplier", "2.0"));

        commandsCoolDown = rootSection.getInt("commands_cooldown", 3);
        repairCoolDown = rootSection.getInt("repair_cooldown", 5);

        stoneFrequency = rootSection.getInt("stone_frequency", 40);

        dropExpToFeet = rootSection.getBoolean("drop_exp_to_feet", true);
        dropItemsToFeet = rootSection.getBoolean("drop_items_to_feet", false);

        multipleStoneDrops = rootSection.getBoolean("multiple_drops", false);
        alwaysDropPrimitiveItem = rootSection.getBoolean("always_drop_primitive", false);
        alwaysUseCustomDrops = rootSection.getBoolean("always_use_custom_drops", false);

        allowHopperDropOutput = rootSection.getBoolean("allow_hopper_output", true);
        allowCoalUpgradesByHopper = rootSection.getBoolean("allow_hopper_input", true);

        return true;
    }

    public int getCommandsCoolDown() {
        return commandsCoolDown;
    }

    public int getRepairCoolDown() {
        return repairCoolDown;
    }

    public long getStoneFrequency() {
        return stoneFrequency;
    }

    public boolean isDropExpToFeet() {
        return dropExpToFeet;
    }

    public boolean isDropItemsToFeet() {
        return dropItemsToFeet;
    }

    public boolean isMultipleStoneDrops() {
        return multipleStoneDrops;
    }

    public boolean isAlwaysDropPrimitiveItem() {
        return alwaysDropPrimitiveItem;
    }

    public boolean isAlwaysUseCustomDrops() {
        return alwaysUseCustomDrops;
    }

    public boolean isAllowHopperDropOutput() {
        return allowHopperDropOutput;
    }

    public boolean isAllowCoalUpgradesByHopper() {
        return allowCoalUpgradesByHopper;
    }

    public float getDefaultDropMultiplier() {
        return defaultDropMultiplier;
    }

    public float getMaxDropMultiplier() {
        return maxDropMultiplier;
    }

}
