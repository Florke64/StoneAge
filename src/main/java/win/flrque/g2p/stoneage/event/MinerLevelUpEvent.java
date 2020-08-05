/*
 * Copyright Go2Play.pl (c) 2020.
 * Program made for Go2Play Skyblock server. It's not allowed to re-distribute the code.
 * Author: FlrQue
 */

package win.flrque.g2p.stoneage.event;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import win.flrque.g2p.stoneage.database.playerdata.StoneMachinePlayerStats;

public class MinerLevelUpEvent extends Event implements Cancellable {

    private boolean isCancelled = false;

    private final StoneMachinePlayerStats playerStats;
    private final int upToLevel;

    public MinerLevelUpEvent(StoneMachinePlayerStats stats, int level) {
        this.playerStats = stats;
        this.upToLevel = level;
    }

    public int getUpToLevel() {
        return upToLevel;
    }

    public StoneMachinePlayerStats getPlayerStats() {
        return playerStats;
    }

    @Override
    public String getEventName() {
        return super.getEventName();
    }

    public static final HandlerList handlers = new HandlerList();

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
