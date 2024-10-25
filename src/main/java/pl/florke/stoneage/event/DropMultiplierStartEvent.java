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

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class DropMultiplierStartEvent extends Event implements Cancellable {

    public static final HandlerList handlers = new HandlerList();

    private final String callerName;
    private final UUID callerUniqueId;

    private final float value;
    private final long startTime;
    private final long timeout;

    private boolean isCancelled = false;

    public DropMultiplierStartEvent(String callerName, UUID callerUniqueId, float value, long startTime, long timeout) {
        this.callerName = callerName;
        this.callerUniqueId = callerUniqueId;

        this.value = value;
        this.startTime = startTime;
        this.timeout = timeout;
    }

    public float getValue() {
        return value;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getTimeout() {
        return timeout;
    }

    public String getCallerName() {
        return callerName;
    }

    public UUID getCallerUniqueId() {
        return callerUniqueId;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
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
        return isCancelled;
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
