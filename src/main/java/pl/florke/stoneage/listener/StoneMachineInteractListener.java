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

import org.bukkit.block.Dispenser;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import pl.florke.stoneage.StoneAge;
import pl.florke.stoneage.gui.Window;
import pl.florke.stoneage.gui.window.StoneMachineWindow;

public class StoneMachineInteractListener implements Listener {

    private final StoneAge plugin;

    public StoneMachineInteractListener() {
        plugin = StoneAge.getPlugin(StoneAge.class);
    }

    @EventHandler
    public void onStoneMachineClick(@NotNull PlayerInteractEvent event) {
        if (event.getPlayer() == null || event.isCancelled())
            return;

        final Player player = event.getPlayer();

        if (!event.getAction().equals(Action.RIGHT_CLICK_BLOCK) || player.isSneaking())
            return;

        if (!plugin.getStoneMachine().isStoneMachine(event.getClickedBlock()))
            return;

        final ItemStack tool = player.getInventory().getItemInMainHand();
        if (plugin.getApplicableTools().isMachineDestroyTool(tool) && player.isOp()) {
            return;
        }

        event.setCancelled(true);

        final Dispenser clickedStoneMachine = (Dispenser) event.getClickedBlock().getState();

        new BukkitRunnable() {
            @Override
            public void run() {
                final Window window = new StoneMachineWindow(player, clickedStoneMachine);

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        window.open(player);
                    }
                }.runTask(plugin);
            }
        }.runTaskAsynchronously(plugin);

    }

}
