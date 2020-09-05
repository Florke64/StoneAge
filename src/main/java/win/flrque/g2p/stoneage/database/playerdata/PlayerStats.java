/*
 * Copyright Go2Play.pl (c) 2020.
 * Program made for Go2Play Skyblock server. It's not allowed to re-distribute the code.
 * Author: FlrQue
 */

package win.flrque.g2p.stoneage.database.playerdata;

import win.flrque.g2p.stoneage.StoneAge;
import win.flrque.g2p.stoneage.drop.DropEntry;
import win.flrque.g2p.stoneage.event.MinerLevelUpEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class PlayerStats {

    private final StoneAge plugin;

    private final UUID uuid;
    private final String playerName;

    private boolean unsavedEdits = false;

    private long minerExp;
    private int minerLvl;

    private final Map<String, Integer> statistics = new HashMap<>();

    public PlayerStats(final UUID uuid, final String playerName) {
        plugin = StoneAge.getPlugin(StoneAge.class);

        this.uuid = uuid;
        this.playerName = playerName;

        this.minerExp = 0;
        this.minerLvl = 1;

        statistics.put(plugin.getDropCalculator().getPrimitiveDropEntry().getEntryName(), 0);
        for(DropEntry entry : plugin.getDropCalculator().getDropEntries()) {
            statistics.put(entry.getEntryName(), 0);
        }
    }

    public void setStatistic(String key, int value) {
        statistics.put(key, value);
    }

    public int increaseStatistic(String key) {
        return increaseStatistic(key, 1);
    }

    public int increaseStatistic(String key, int amount) {
        final int value = getStatistic(key) + amount;
        statistics.put(key, value);
        unsavedEdits = true;

        return value;
    }

    public int getStatistic(String key) {
        return statistics.getOrDefault(key, 0);
    }

    public Set<String> getStatisticKeys() {
        return statistics.keySet();
    }

    public String getPlayerName() {
        return playerName;
    }

    public UUID getUniqueId() {
        return uuid;
    }

    public boolean hasUnsavedEdits() {
        return unsavedEdits;
    }

    public void onDatabaseSave() {
        unsavedEdits = false;
    }

    public void addMinerExp(final long expAmount) {
        setMinerExp(this.minerExp + expAmount, true);
    }

    public void setMinerExp(final long minerExp, final boolean updateLevel) {
        final int updatedLevel = plugin.getExpCalculator().expToLevel(minerExp);

        if(updateLevel && updatedLevel > this.minerLvl) {
            setMinerLvl(updatedLevel, true);
        }

        this.minerExp = minerExp;

        this.unsavedEdits = true;
    }

    public long getMinerExp() {
        return this.minerExp;
    }

    public void setMinerLvl(final int minerLvl, final boolean callEvent) {
        if(callEvent) {
            final MinerLevelUpEvent event = new MinerLevelUpEvent(this, minerLvl);
            this.plugin.getServer().getPluginManager().callEvent(event);

            if (event.isCancelled())
                return;
        }

        this.minerLvl = minerLvl;

        this.unsavedEdits = true;
    }

    public int getMinerLvl() {
        return minerLvl;
    }

}
