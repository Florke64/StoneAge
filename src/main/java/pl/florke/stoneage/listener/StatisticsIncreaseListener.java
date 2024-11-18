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
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import pl.florke.stoneage.StoneAge;
import pl.florke.stoneage.database.playerdata.PlayerStats;
import pl.florke.stoneage.drop.DropEntry;
import pl.florke.stoneage.drop.DropLoot;
import pl.florke.stoneage.event.StoneMachineStoneBreakEvent;

import java.util.Collection;
import java.util.UUID;

public class StatisticsIncreaseListener implements Listener {

    private final StoneAge plugin;

    public StatisticsIncreaseListener() {
        this.plugin = StoneAge.getPlugin(StoneAge.class);
    }

    @EventHandler
    public void onStoneMachineUse(@NotNull StoneMachineStoneBreakEvent event) {
        final Player player = event.getPlayer();

        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) {
            return;
        }

        final UUID playerUUID = player.getUniqueId();
        final PlayerStats playerStats = plugin.getPlayersData().getPlayerStoneMachineStats(playerUUID);

        final DropLoot dropLoot = event.getLoot();
        if (dropLoot == null) {
            return;
        }

        final Collection<DropEntry> dropEntries = dropLoot.getActiveDropEntries();

        for (DropEntry entry : dropEntries) {
            final int amount = event.getLoot().getAmountLooted(entry);
            playerStats.increaseStatistic(entry.getKey(), amount);
        }
    }

}
