/*
 * Copyright Go2Play.pl (c) 2020.
 * Program made for Go2Play Skyblock server. It's not allowed to re-distribute the code.
 * Author: FlrQue
 */

package win.flrque.g2p.stoneage.database;

import org.bukkit.Bukkit;
import win.flrque.g2p.stoneage.machine.PersonalDropConfig;
import win.flrque.g2p.stoneage.machine.StoneMachinePlayerStats;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerSetupManager {

    private final Map<UUID, PersonalDropConfig> playerPersonalDropConfig = new HashMap<>();
    private final Map<UUID, StoneMachinePlayerStats> stoneMachinePlayerStats = new HashMap<>();

    public StoneMachinePlayerStats getPlayerStoneMachineStats(UUID uuid) {
        if(!stoneMachinePlayerStats.containsKey(uuid)) {
            stoneMachinePlayerStats.put(uuid, createStoneMachinePlayerStats(uuid));
        }

        return stoneMachinePlayerStats.get(uuid);
    }

    private StoneMachinePlayerStats createStoneMachinePlayerStats(UUID uuid) {
        final String playerName = Bukkit.getOfflinePlayer(uuid).getName();
        final StoneMachinePlayerStats stats = new StoneMachinePlayerStats(uuid, playerName);
        stoneMachinePlayerStats.put(uuid, stats);

        return stoneMachinePlayerStats.get(uuid);
    }

    public PersonalDropConfig getPersonalDropConfig(UUID uuid) {
        if(!playerPersonalDropConfig.containsKey(uuid)) {
            createPersonalDropConfig(uuid);
        }

        return playerPersonalDropConfig.get(uuid);
    }

//    public void loadPersonalDropConfigs() {
//        //TODO: Load all from DataBase on server start
//    }

    private PersonalDropConfig createPersonalDropConfig(UUID uuid) {
        final String playerName = Bukkit.getOfflinePlayer(uuid).getName();
        final PersonalDropConfig config = new PersonalDropConfig(uuid, playerName);
        playerPersonalDropConfig.put(uuid, config);

        return playerPersonalDropConfig.get(uuid);
    }

}
