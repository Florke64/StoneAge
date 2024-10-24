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
import org.bukkit.block.Dispenser;
import org.bukkit.block.data.Directional;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.jetbrains.annotations.NotNull;
import pl.florke.stoneage.StoneAge;
import pl.florke.stoneage.machine.StoneMachine;

public class StoneMachinePlaceListener implements Listener {

    private final StoneAge plugin;

    public StoneMachinePlaceListener() {
        plugin = StoneAge.getPlugin(StoneAge.class);
    }

    @EventHandler
    public void onStoneMachinePlace(@NotNull BlockPlaceEvent event) {
        if (event.isCancelled())
            return;

        final Block placedBlock = event.getBlockPlaced();

        if (!plugin.getStoneMachine().isStoneMachine(event.getBlock()))
            return;

        final Directional machine = (Directional) placedBlock.getState().getBlockData();
        final Location stoneGenerationLocation = placedBlock.getRelative(machine.getFacing(), 1).getLocation();

        final Dispenser stoneMachine = (Dispenser) placedBlock.getState();
        stoneMachine.getInventory().setItem(StoneMachine.MACHINE_LABEL_SLOT, plugin.getStoneMachine().getMachineLabel());
        stoneMachine.setCustomName(plugin.getStoneMachine().getMachineName());

        plugin.getStoneMachine().generateStone(stoneGenerationLocation);
    }

}
