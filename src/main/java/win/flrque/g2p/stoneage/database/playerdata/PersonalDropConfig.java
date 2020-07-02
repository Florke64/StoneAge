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
import java.util.UUID;

public class PersonalDropConfig {

    private final StoneAge plugin;

    private final UUID uuid;
    private final String playerName;

    private final Map<DropEntry, Boolean> customDropEntries = new HashMap<>();

    public PersonalDropConfig(final UUID uuid, final String playerName) {
        plugin = StoneAge.getPlugin(StoneAge.class);

        this.uuid = uuid;
        this.playerName = playerName;

        customDropEntries.put(plugin.getDropCalculator().getPrimitiveDropEntry(), true);
        for(DropEntry drop : plugin.getDropCalculator().getDropEntries()) {
            customDropEntries.put(drop, true);
        }
    }

    public boolean isDropping(DropEntry dropEntry) {
        return customDropEntries.containsKey(dropEntry) && customDropEntries.get(dropEntry).booleanValue();
    }

    public void setDropEntry(DropEntry dropEntry, boolean shouldDrop) {
        customDropEntries.put(dropEntry, shouldDrop);
    }

    public boolean switchDropEntry(DropEntry dropEntry) {
        setDropEntry(dropEntry, !isDropping(dropEntry));

        return isDropping(dropEntry);
    }

}
