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

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import pl.florke.stoneage.StoneAge;
import pl.florke.stoneage.database.playerdata.PlayerStats;
import pl.florke.stoneage.event.MinerLevelUpEvent;
import pl.florke.stoneage.util.Message;

import java.util.UUID;

public class MinerLevelUpListener implements Listener {

    private final StoneAge plugin;

    public MinerLevelUpListener() {
        this.plugin = StoneAge.getPlugin(StoneAge.class);
    }

    @EventHandler
    public void onPlayerLevelUp(@NotNull MinerLevelUpEvent event) {
        if (event.isCancelled())
            return;

        final PlayerStats stats = event.getPlayerStats();
        final UUID playerUniqueId = stats.getUniqueId();
        final Player player = this.plugin.getServer().getPlayer(playerUniqueId);
        if (player == null || !player.isOnline()) {
            return;
        }

        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);

        final int lvl = event.getUpToLevel();
        if (lvl > 40 || lvl % 5 == 0 || lvl == 2) {
            final Message congratulations = new Message("&dGratulacje! &c$_1 &dosiaga &6$_2 &dpoziom gornictwa!");
            congratulations.placeholder(1, player.getName());
            congratulations.placeholder(2, Integer.toString(lvl));
            congratulations.broadcast();
        } else {
            final Message congratulations = new Message("&dGratulacje! &c$_1 &dosiagasz &6$_2 &dpoziom gornictwa!");
            congratulations.placeholder(1, player.getName());
            congratulations.placeholder(2, Integer.toString(lvl));
            congratulations.send(player);
        }
    }
}
