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

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import pl.florke.stoneage.util.Message;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

public class ToolsConfigReader extends ConfigSectionReader {

    private final Map<Material, Integer> miningTools = new HashMap<>();
    private Material machineDestroyTool;

    public ToolsConfigReader(ConfigurationSection configurationSection) {
        super(configurationSection);
        this.machineDestroyTool = Material.GOLDEN_PICKAXE;
    }

    public void compile() {
        final String machineDestroyToolName = rootSection.getString("machine_destroy");
        final Material machineDestroyToolMaterial = Material.getMaterial(machineDestroyToolName);
        if (machineDestroyToolMaterial != null) {
            this.machineDestroyTool = machineDestroyToolMaterial;
        }

        final ConfigurationSection applicableToolsSection = rootSection.getConfigurationSection("levels");
        if (applicableToolsSection == null) {
            new Message("No applicable tools found. Please, check config file!").log(Level.WARNING);
            return;
        }

        for (final String key : applicableToolsSection.getKeys(false)) {
            final Material toolMaterial = Material.getMaterial(key);
            if (toolMaterial == null) {
                new Message("Invalid $_1 in tool levels configuration! Skipping...")
                        .placeholder(1, key).log(Level.WARNING);
                continue;
            }

            final int level = applicableToolsSection.getInt(key, 1);
            miningTools.put(toolMaterial, level);

            new Message("Added applicable tool $_1 ($_2)")
                    .placeholder(1, toolMaterial.toString())
                    .placeholder(2, String.valueOf(level))
                    .log(Level.INFO);
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
