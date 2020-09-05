/*
 * Copyright Go2Play.pl (c) 2020.
 * Program made for Go2Play Skyblock server. It's not allowed to re-distribute the code.
 * Author: FlrQue
 */

package win.flrque.g2p.stoneage.config;

import org.bukkit.configuration.ConfigurationSection;

public class GeneralConfigReader extends ConfigSectionReader {

    //Drop multiplier settings
    private float defaultDropMultiplier, maxDropMultiplier;
    private int maxMinerLevel;

    //Values in seconds
    private int commandsCoolDown, repairCoolDown;

    //Value in game ticks
    private long stoneFrequency;

    //Life-ease functionalities
    private boolean dropExpToFeet, dropItemsToFeet;

    //Redstone-like interactions with stone machine
    private boolean allowHopperDropOutput;
    private boolean allowCoalUpgradesByHopper;

    public GeneralConfigReader(ConfigurationSection section) {
        super(section);
    }

    public boolean compile() {
        defaultDropMultiplier = Float.parseFloat(rootSection.getString("default_drop_multiplier", "1.0"));
        maxDropMultiplier = Float.parseFloat(rootSection.getString("max_drop_multiplier", "2.0"));

        maxMinerLevel = rootSection.getInt("max_miner_level", 99);

        commandsCoolDown = rootSection.getInt("commands_cooldown", 3);
        repairCoolDown = rootSection.getInt("repair_cooldown", 5);

        stoneFrequency = rootSection.getInt("stone_frequency", 40);

        dropExpToFeet = rootSection.getBoolean("drop_exp_to_feet", true);
        dropItemsToFeet = rootSection.getBoolean("drop_items_to_feet", false);

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

    public int getMaxMinerLevel() {
        return maxMinerLevel;
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
