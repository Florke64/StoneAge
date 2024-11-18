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

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.TileState;
import org.bukkit.block.data.Directional;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import pl.florke.stoneage.StoneAge;
import pl.florke.stoneage.drop.DropEntry;
import pl.florke.stoneage.util.Message;

import java.util.*;
import java.util.logging.Level;

public class ResourceSpawner {

    private final StoneAge plugin = StoneAge.getPlugin(StoneAge.class);

    private long spawnFrequency = 40L;

    // Map <Primitive drop's key; List of allowed custom drop's keys>
    private final Map<DropEntry, List<DropEntry>> resourceRelationship = new HashMap<>();

    protected ResourceSpawner() {

    }

    public void addResourceChild(final DropEntry parent, final DropEntry child) {
        if (Objects.isNull(parent) || Objects.isNull(child)) {
            new Message("Resource relations not configured properly! Check names.").log(Level.WARNING);
            return;
        }

        new Message("Adding " + child.getKey().getKey() + " as a child of " + parent.getKey().getKey()).log(Level.INFO);

        if (!resourceRelationship.containsKey(parent))
            resourceRelationship.put(parent, new ArrayList<>());

        final List<DropEntry> relations = resourceRelationship.get(parent);
        relations.add(child);

        resourceRelationship.put(parent, relations);
    }

    public boolean isResourceChild(final DropEntry parent, final DropEntry child) {
        return resourceRelationship.containsKey(parent)
                && resourceRelationship.get(parent).contains(child);
    }

    public boolean isRegisteredResource(final DropEntry dropEntry) {
        return resourceRelationship.containsKey(dropEntry);
    }

    public List<DropEntry> getResourceChildren(final DropEntry parent) {
        final List<DropEntry> children = new ArrayList<>();
        if (resourceRelationship.containsKey(parent))
            children.addAll(resourceRelationship.get(parent));

        return children;
    }

    /**
     * @see ResourceSpawner#spawnResource(Location, DropEntry, long)
     */
    public void spawnResource(@NotNull final Location location) {
        DropEntry primitive = plugin.getDropCalculator().calculatePrimitive();
        spawnResource(location, primitive, spawnFrequency);
    }

    /**
     * This method will wait given delay in async task, switch to the sync task
     * and then check if given location is connected to any Stone Machine block.
     * Will also verify if given location is "empty" (meaning, no blocks were placed in mean time).<br />
     * If everything is fine, then block type at given position is set to appropriate loot material.
     *
     * @param location where stone block will be placed.
     * @param delay    value in server ticks to wait and place block.
     */
    private void spawnResource(@NotNull final Location location, @NotNull final DropEntry primitive, final long delay) {
        final Block block = Objects.requireNonNull(location.getWorld()).getBlockAt(location);

        //Returning to the Main thread
        new BukkitRunnable() {
            @Override
            public void run() {
                final StoneMachine stoneMachine = plugin.getStoneMachine();

                if (!stoneMachine.isConnectedToStoneMachine(block))
                    return;

                if (!block.isEmpty())
                    return;

                block.setType(primitive.getBlockMaterial(), true);
                block.setBlockData(primitive.getBlockMaterial().createBlockData(), true);
                block.getState().update(true);
            }
        }.runTaskLater(plugin, delay);
    }

    /**
     * Gets {@link Location} where stone block should be generated for given parameter.
     *
     * @param machineState {@link TileState} instance of a stone machine.
     * @return where stone block should be generated for a stone machine given in a parameter.
     */
    public Location getGeneratedResourceLocation(@NotNull final TileState machineState) {
        final StoneMachine stoneMachine = plugin.getStoneMachine();
        if (!stoneMachine.isStoneMachine(machineState))
            return null;

        final Directional machine = (Directional) machineState.getBlockData();
        return machineState.getBlock().getRelative(machine.getFacing()).getLocation();
    }

    /**
     * 'machines.frequency' in the config.yml file.
     *
     * @param spawnFrequency time in which stone blocks will regenerate after breaking (in server ticks).
     */
    public void setSpawnFrequency(final long spawnFrequency) {
        this.spawnFrequency = spawnFrequency < 0 ? 0 : spawnFrequency;
    }
}
