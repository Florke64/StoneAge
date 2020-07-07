/*
 * Copyright Go2Play.pl (c) 2020.
 * Program made for Go2Play Skyblock server. It's not allowed to re-distribute the code.
 * Author: FlrQue
 */

package win.flrque.g2p.stoneage.event;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

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
    public String getEventName() {
        return super.getEventName();
    }

    @Override
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
