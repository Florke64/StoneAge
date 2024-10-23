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

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Dispenser;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import pl.florke.stoneage.StoneAge;
import pl.florke.stoneage.gui.Window;
import pl.florke.stoneage.gui.WindowManager;
import pl.florke.stoneage.util.Message;

public class StoneMachineBreakListener implements Listener {

    private final StoneAge plugin;

    public StoneMachineBreakListener() {
        plugin = StoneAge.getPlugin(StoneAge.class);
    }

    @EventHandler
    public void onStoneMachineBreak(@NotNull BlockBreakEvent event) {
        if (event.isCancelled())
            return;

        if (event.getPlayer() == null)
            return;

        final Player destroyer = event.getPlayer();
        final GameMode gameMode = destroyer.getGameMode();

        if (!plugin.getStoneMachine().isStoneMachine(event.getBlock()))
            return;

        final Block brokenBlock = event.getBlock();
        final Dispenser stoneMachine = (Dispenser) brokenBlock.getState();

        event.setDropItems(false);

        final ItemStack tool = destroyer.getInventory().getItemInMainHand();
        if (plugin.getApplicableTools().isMachineDestroyTool(tool) || gameMode == GameMode.CREATIVE) {
            final Location brokenBlockLocation = brokenBlock.getLocation();

            stoneMachine.getInventory().clear();

            //Closing all active windows
            final WindowManager windowManager = plugin.getWindowManager();
            final Window brokenMachinesWindow = windowManager.getWindow(stoneMachine);
            if (brokenMachinesWindow != null) {

                final Message machineDestroyedMsg = new Message(plugin.getLanguage("stone-machine-destroyed"));
                for (Player user : brokenMachinesWindow.getUsers()) {
                    if (user != null && user.isOnline()) {

                        //TODO: To be tested in action
                        if (user.getOpenInventory() == null)
                            continue;

                        if (windowManager.getWindow(user).getBukkitInventory().equals(user.getOpenInventory().getTopInventory())) {
                            user.closeInventory();
                            machineDestroyedMsg.send(destroyer);
                        }
                    }
                }
            }

            if (gameMode != GameMode.CREATIVE) {
                brokenBlockLocation.getWorld().dropItemNaturally(brokenBlockLocation, plugin.getStoneMachine().createStoneMachineItem());
            }

        } else {
            event.setCancelled(true);

            final Message wrongToolMsg = new Message(plugin.getLanguage("stone-machine-destroyed-deny"));
            wrongToolMsg.send(destroyer);
        }
    }

}
