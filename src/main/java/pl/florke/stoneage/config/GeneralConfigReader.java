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

package pl.florke.stoneage.config;

import org.bukkit.configuration.ConfigurationSection;
import pl.florke.stoneage.StoneAge;
import pl.florke.stoneage.util.Message;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class GeneralConfigReader extends ConfigSectionReader {

    private final HashMap<String, ArrayList<String>> resourceRelations = new HashMap<>();

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

        try {
            compile();
        } catch (RuntimeException ex) {
            new Message("Invalid configuration!", ex.getMessage()).log(Level.SEVERE);
        }
    }

    private void compile() {
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

        readResourceRelations();
    }

    private void readResourceRelations() {
        final ConfigurationSection resourcesSection = StoneAge.getPlugin(StoneAge.class)
                .getConfig().getConfigurationSection("resources");
        if (resourcesSection == null) {
            new Message("Invalid configuration, missing `resources` section!").log(Level.SEVERE);
            return;
        }

        for (String key : resourcesSection.getKeys(false)) {
            ArrayList<String> resources = new ArrayList<>(resourcesSection.getStringList(key));
            resourceRelations.put(key, resources);
        }
    }

    public Map<String, ArrayList<String>> getResourceRelations() {
        return new HashMap<>(resourceRelations);
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
