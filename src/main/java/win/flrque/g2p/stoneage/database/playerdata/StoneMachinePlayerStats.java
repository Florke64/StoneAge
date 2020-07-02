/*
 * Copyright Go2Play.pl (c) 2020.
 * Program made for Go2Play Skyblock server. It's not allowed to re-distribute the code.
 * Author: FlrQue
 */

package win.flrque.g2p.stoneage.database.playerdata;

import win.flrque.g2p.stoneage.StoneAge;
import win.flrque.g2p.stoneage.drop.DropEntry;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class StoneMachinePlayerStats {

    private final StoneAge plugin;

    private final UUID uuid;
    private final String playerName;

    private final Map<String, Integer> statistics = new HashMap<>();

    public StoneMachinePlayerStats(final UUID uuid, final String playerName) {
        plugin = StoneAge.getPlugin(StoneAge.class);

        this.uuid = uuid;
        this.playerName = playerName;

        statistics.put(plugin.getDropCalculator().getPrimitiveDropEntry().getEntryName(), 0);
        for(DropEntry entry : plugin.getDropCalculator().getDropEntries()) {
            statistics.put(entry.getEntryName(), 0);
        }
    }

    public void setStatistic(String key, int value) {
        statistics.put(key, value);
    }

    public int increaseStatistic(String key) {
        final int value = getStatistic(key)+1;
        statistics.put(key, value);

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
}
