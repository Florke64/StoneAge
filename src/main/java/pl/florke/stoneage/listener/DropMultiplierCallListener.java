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

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import pl.florke.stoneage.StoneAge;
import pl.florke.stoneage.event.DropMultiplierStartEvent;
import pl.florke.stoneage.util.Message;

import java.sql.SQLException;
import java.util.UUID;
import java.util.logging.Level;

public class DropMultiplierCallListener implements Listener {

    private final StoneAge plugin;

    public DropMultiplierCallListener() {
        this.plugin = StoneAge.getPlugin(StoneAge.class);
    }

    @EventHandler
    public void onDropMultiplierCall(@NotNull final DropMultiplierStartEvent event) {
        if (event.isCancelled()) {
            return;
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                final String callerName = event.getCallerName();
                final UUID callerUUID = event.getCallerUniqueId();
                final float value = event.getValue();
                final long startMillis = event.getStartTime();
                final long timeoutTime = event.getTimeout();

                try {
                    plugin.getDatabaseController().insertDropMultiplierRecord(callerName, callerUUID, value, startMillis, timeoutTime);
                } catch (SQLException ex) {
                    new Message("Unable to save multiplier data into the database! Multiplier won't recover after server restart!").log(Level.WARNING);
                    ex.printStackTrace();
                }
            }
        }.runTaskAsynchronously(plugin);

        for (final Player player : Bukkit.getOnlinePlayers()) {
            plugin.getDropCalculator().getDropMultiplier().getMultiplierBossBar().addPlayer(player);
        }

    }

}
