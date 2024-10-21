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

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ApplicableTools {

    private final Map<Material, Integer> toolLevelMap = new HashMap<>();

    private final Material machineDestroyTool;

    public ApplicableTools(@NotNull final Material machineDestroyTool) {
        this.machineDestroyTool = machineDestroyTool;
    }

    public Material getMachineDestroyTool() {
        return machineDestroyTool;
    }

    @SuppressWarnings("unused")
    public Set<Material> getApplicableToolsSet() {
        return toolLevelMap.keySet();
    }

    public boolean isApplicableTool(@NotNull final Material tool) {
        return toolLevelMap.containsKey(tool);
    }

    public int getToolLevel(@NotNull final ItemStack tool) {
        return getToolLevel(tool.getType());
    }

    public int getToolLevel(@NotNull final Material tool) {
        return toolLevelMap.getOrDefault(tool, 0);
    }

    public void addApplicableTool(@NotNull final Material tool, int level) {
        toolLevelMap.put(tool, level);
    }

    public boolean isMachineDestroyTool(@NotNull final ItemStack tool) {
        return isMachineDestroyTool(tool.getType());
    }

    public boolean isMachineDestroyTool(@NotNull final Material tool) {
        return tool == getMachineDestroyTool();
    }

}
