/*
 * Copyright Go2Play.pl (c) 2020.
 * Program made for Go2Play Skyblock server. It's not allowed to re-distribute the code.
 * Author: FlrQue
 */

package win.flrque.g2p.stoneage.machine;

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
