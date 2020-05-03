/*
 * Copyright Go2Play.pl (c) 2020.
 * Program made for Go2Play Skyblock server. It's not allowed to re-distribute the code.
 * Author: FlrQue
 */

package win.flrque.g2p.stoneage.drop;

import org.bukkit.OfflinePlayer;
import win.flrque.g2p.stoneage.StoneAge;

import java.util.HashMap;
import java.util.Map;

public class PersonalDropConfig {

    private final StoneAge plugin;

    private final OfflinePlayer player;

    private final Map<DropEntry, Boolean> customDropEntries = new HashMap<>();

    public PersonalDropConfig(OfflinePlayer player) {
        plugin = StoneAge.getPlugin(StoneAge.class);

        this.player = player;

        customDropEntries.put(plugin.getDropCalculator().getPrimitiveDrop(), true);
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

}
