/*
 * Copyright Go2Play.pl (c) 2020.
 * Program made for Go2Play Skyblock server. It's not allowed to re-distribute the code.
 * Author: FlrQue
 */

package win.flrque.g2p.stoneage.drop;

import win.flrque.g2p.stoneage.StoneAge;

public class DropMultiplier {

    private final StoneAge plugin;

    private final float defaultDropMultiplier;
    private final float maxDropMultiplier;

    private float currentDropMultiplier;
    private long multiplierTimeout = 0;

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

    public boolean setDropMultiplier(float value, long time) {
        if(value <= defaultDropMultiplier || value > maxDropMultiplier)
            return false;

        if(time < (1 * 60 * 1000) || time > (24 * 60 * 60 * 1000))
            return false;

        setCurrentDropMultiplier(value);
        setMultiplierTimeout(System.currentTimeMillis() + time);

        return true;
    }

    public boolean isActive() {
        if(getMultiplierTimeout() < System.currentTimeMillis())
            return false;

        return defaultDropMultiplier != currentDropMultiplier;
    }

}
