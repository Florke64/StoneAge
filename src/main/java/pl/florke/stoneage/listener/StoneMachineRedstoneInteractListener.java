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

import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseEvent;
import org.jetbrains.annotations.NotNull;
import pl.florke.stoneage.StoneAge;

public class StoneMachineRedstoneInteractListener implements Listener {

    private final StoneAge plugin;

    public StoneMachineRedstoneInteractListener() {
        plugin = StoneAge.getPlugin(StoneAge.class);
    }

    @EventHandler
    public void onRedstoneInteract(@NotNull BlockDispenseEvent event) {
        if (event.isCancelled())
            return;

        final Block interactedBlock = event.getBlock();
        if (!plugin.getStoneMachine().isStoneMachine(interactedBlock))
            return;

        event.setCancelled(true);

        /*This code is ready and provides stone generation on Redstone input
         *   but this can be easily abused and lag a server!
         * TODO: add possibility to disable this functionality with config.yml
         */
//        final Dispenser stoneMachine = (Dispenser) interactedBlock.getState();
//        final Location stoneLocation = plugin.getStoneMachine().getGeneratedStoneLocation(stoneMachine);
//
//        plugin.getStoneMachine().generateStone(stoneLocation, 0);
    }

}
