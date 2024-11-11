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

package pl.florke.stoneage.event;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import pl.florke.stoneage.database.playerdata.PlayerStats;

public class MinerLevelUpEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    private final PlayerStats playerStats;
    private final int upToLevel;
    private boolean isCancelled = false;

    public MinerLevelUpEvent(PlayerStats stats, int level) {
        this.playerStats = stats;
        this.upToLevel = level;
    }

    public int getUpToLevel() {
        return upToLevel;
    }

    public PlayerStats getPlayerStats() {
        return playerStats;
    }


    // SpotBugs highlights that handlers might be null despite @NotNull annotation
    // But Bukkit requires @NotNull annotation for overwritten getHandlers() method
    @SuppressFBWarnings("NP_NONNULL_RETURN_VIOLATION")
    @Override @NotNull
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    /**
     * Gets the cancellation state of this event. A cancelled event will not
     * be executed in the server, but will still pass to other plugins
     *
     * @return true if this event is cancelled
     */
    @Override
    public boolean isCancelled() {
        return this.isCancelled;
    }

    /**
     * Sets the cancellation state of this event. A cancelled event will not
     * be executed in the server, but will still pass to other plugins.
     *
     * @param cancel true if you wish to cancel this event
     */
    @Override
    public void setCancelled(boolean cancel) {
        this.isCancelled = cancel;
    }
}
