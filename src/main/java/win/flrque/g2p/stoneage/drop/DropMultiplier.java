/*
 * Copyright Go2Play.pl (c) 2020.
 * Program made for Go2Play Skyblock server. It's not allowed to re-distribute the code.
 * Author: FlrQue
 */

package win.flrque.g2p.stoneage.drop;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import win.flrque.g2p.stoneage.StoneAge;
import win.flrque.g2p.stoneage.event.DropMultiplierStartEvent;

import java.util.UUID;

public class DropMultiplier {

    private final StoneAge plugin;

    private final float defaultDropMultiplier;
    private final float maxDropMultiplier;

    private float currentDropMultiplier;
    private long multiplierTimeout = 0;
    private long multiplierSetOn = 0;

    private String callerName;
    private UUID callerUniqueId;

    public DropMultiplier(float defaultDropMultiplier, float maxDropMultiplier) {
        plugin = StoneAge.getPlugin(StoneAge.class);

        this.defaultDropMultiplier = defaultDropMultiplier;
        this.currentDropMultiplier = defaultDropMultiplier;
        this.maxDropMultiplier = maxDropMultiplier;
    }

    public float getDefaultDropMultiplier() {
        return defaultDropMultiplier;
    }

    public float getMaxDropMultiplier() {
        return maxDropMultiplier;
    }

    public float getCurrentDropMultiplier() {
        return currentDropMultiplier;
    }

    private void setCurrentDropMultiplier(float currentDropMultiplier) {
        this.currentDropMultiplier = currentDropMultiplier;
    }

    public long getMultiplierTimeout() {
        return multiplierTimeout;
    }

    private void setMultiplierTimeout(long multiplierTimeout) {
        this.multiplierTimeout = multiplierTimeout;
    }

    public long getMultiplierStartTime() {
        return multiplierSetOn;
    }

    private void setMultiplierStartTime(long multiplierSetOn) {
        this.multiplierSetOn = multiplierSetOn;
    }

    public UUID getCallerUniqueId() {
        return callerUniqueId;
    }

    public void setCallerUniqueId(UUID callerUniqueId) {
        this.callerUniqueId = callerUniqueId;
    }

    public String getCallerName() {
        return callerName;
    }

    public void setCallerName(String callerName) {
        this.callerName = callerName;
    }

    public boolean setDropMultiplier(@NotNull CommandSender caller, float value, long time) {
        final String callerName = caller.getName();
        final UUID callerUniqueId;

        if(caller instanceof Player) {
            callerUniqueId = ((Player) caller).getUniqueId();
        } else {
            callerUniqueId = UUID.randomUUID();
        }

        return setDropMultiplier(callerName, callerUniqueId, value, time);
    }

    public boolean setDropMultiplier(String callerName, UUID callerUniqueId, float value, long time) {
        if(value <= defaultDropMultiplier || value > maxDropMultiplier)
            return false;

        if(time < (1 * 60 * 1000) || time > (24 * 60 * 60 * 1000))
            return false;

        final long startTime = System.currentTimeMillis();
        final long timeout = System.currentTimeMillis() + time;

        DropMultiplierStartEvent event = new DropMultiplierStartEvent(callerName, callerUniqueId, value, startTime, timeout);
        plugin.getServer().getPluginManager().callEvent(event);

        if(event.isCancelled()) {
            return false;
        }

        setCurrentDropMultiplier(value);
        setMultiplierStartTime(startTime);
        setMultiplierTimeout(timeout);

        setCallerName(callerName);
        setCallerUniqueId(callerUniqueId);

        return true;
    }

    public int getMinutesLeft() {
        return (int)((getMultiplierTimeout()/1000 - System.currentTimeMillis()/1000)/60);
    }

    public boolean isActive() {
        if(getMultiplierTimeout() < System.currentTimeMillis())
            return false;

        return defaultDropMultiplier != currentDropMultiplier;
    }

    /* SELECT * FROM `StoneAge_DropMultiplier` WHERE `StoneAge_DropMultiplier`.`Timeout` > CURRENT_TIMESTAMP; */
    public void readPreviousMultiplierFromDatabase() {
        //TODO: Query database entry and apply if there is anything relatable
    }

}
