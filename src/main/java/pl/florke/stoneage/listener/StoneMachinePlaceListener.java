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

package pl.florke.stoneage.listener;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.TileState;
import org.bukkit.block.data.Directional;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import pl.florke.stoneage.StoneAge;

public class StoneMachinePlaceListener implements Listener {

    private final StoneAge plugin;

    public StoneMachinePlaceListener() {
        plugin = StoneAge.getPlugin(StoneAge.class);
    }

    @EventHandler
    public void onStoneMachinePlace(@NotNull BlockPlaceEvent event) {
        if (event.isCancelled())
            return;

        final ItemStack itemInHand = event.getItemInHand();
        if (!plugin.getStoneMachine().isStoneMachine(itemInHand))
            return;

        final Block placedBlock = event.getBlock();

        if (!(placedBlock.getState() instanceof TileState machineState)
                || !(placedBlock.getState().getBlockData() instanceof Directional directionalMachine)) {
            return;
        }

        final Location stoneGenerationLocation = placedBlock.getRelative(directionalMachine.getFacing(), 1).getLocation();

        final PersistentDataContainer machineData = machineState.getPersistentDataContainer();
        machineData.set(plugin.getStoneMachine().getMachineIdentifierKey(), PersistentDataType.BOOLEAN, true);
        machineState.update();

        plugin.getStoneMachine().getResourceSpawner().spawnResource(stoneGenerationLocation);
    }

}
