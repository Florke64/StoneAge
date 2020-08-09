/*
 * Copyright Go2Play.pl (c) 2020.
 * Program made for Go2Play Skyblock server. It's not allowed to re-distribute the code.
 * Author: FlrQue
 */

package win.flrque.g2p.stoneage.util;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import win.flrque.g2p.stoneage.StoneAge;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

public class ConfigSectionTools extends ConfigSectionReader {

    private final StoneAge plugin;

    private Map<Material, Integer> miningTools = new HashMap<>();
    private Material machineDestroyTool;

    public ConfigSectionTools(ConfigurationSection configurationSection) {
        super(configurationSection);

        this.plugin = StoneAge.getPlugin(StoneAge.class);

        this.machineDestroyTool = Material.GOLDEN_PICKAXE;
    }

    public void compile() {
        final String machineDestroyToolName = rootSection.getString("machine_destroy");
        final Material machineDestroyToolMaterial = Material.getMaterial(machineDestroyToolName);
        if(machineDestroyToolMaterial != null) {
            this.machineDestroyTool = machineDestroyToolMaterial;
        }

        final ConfigurationSection applicableToolsSection = rootSection.getConfigurationSection("levels");
        if(applicableToolsSection == null) return;

        for(String key : applicableToolsSection.getKeys(false)) {
            final Material toolMaterial = Material.getMaterial(machineDestroyToolName);
            if(toolMaterial == null) {
                this.plugin.getLogger().log(Level.WARNING, "Invalid \"" + key + "\" in tool levels configuration! Skipping...");
                continue;
            }

            final int level = applicableToolsSection.getInt(key, 1);
            miningTools.put(toolMaterial, level);
        }
    }

    public Material getMachineDestroyTool() {
        return machineDestroyTool;
    }

    public Set<Material> getMiningTools() {
        return miningTools.keySet();
    }

    public int getToolLevel(@NotNull final Material toolMaterial) {
        return miningTools.getOrDefault(toolMaterial, 0);
    }

}
