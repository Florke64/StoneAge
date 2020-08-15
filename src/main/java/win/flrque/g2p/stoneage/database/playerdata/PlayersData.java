/*
 * Copyright Go2Play.pl (c) 2020.
 * Program made for Go2Play Skyblock server. It's not allowed to re-distribute the code.
 * Author: FlrQue
 */

package win.flrque.g2p.stoneage.database.playerdata;

import org.bukkit.Bukkit;
import win.flrque.g2p.stoneage.StoneAge;
import win.flrque.g2p.stoneage.database.SQLManager;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class PlayersData {

    private final StoneAge plugin;

    private final Map<UUID, PlayerConfig> playerPersonalDropConfig = new HashMap<>();
    private final Map<UUID, PlayerStats> stoneMachinePlayerStats = new HashMap<>();

    public PlayersData() {
        plugin = StoneAge.getPlugin(StoneAge.class);
    }

    public PlayerStats getPlayerStoneMachineStats(UUID uuid) {
        if(!stoneMachinePlayerStats.containsKey(uuid)) {
            stoneMachinePlayerStats.put(uuid, createStoneMachinePlayerStats(uuid));
        }

        return stoneMachinePlayerStats.get(uuid);
    }

    private PlayerStats createStoneMachinePlayerStats(UUID uuid) {
        final String playerName = Bukkit.getOfflinePlayer(uuid).getName();
        final PlayerStats stats = new PlayerStats(uuid, playerName);
        stoneMachinePlayerStats.put(uuid, stats);

        return stoneMachinePlayerStats.get(uuid);
    }

    public PlayerConfig getPersonalDropConfig(UUID uuid) {
        if(!playerPersonalDropConfig.containsKey(uuid)) {
            createPersonalDropConfig(uuid);
        }

        return playerPersonalDropConfig.get(uuid);
    }

    public void loadPersonalStoneStatsFromDatabase() {
        final String databaseName = plugin.getDatabaseController().getDatabaseName();
        final String queryStatement = "SELECT * FROM " +databaseName+ ".`" + SQLManager.TABLE_PLAYER_STATS + "`";

        ResultSet result = null;

        try {
            result = plugin.getDatabaseController().runQuery(queryStatement);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if(result == null) {
            plugin.getLogger().log(Level.SEVERE, "Couldn't load Personal Stone Stats on start!");
            return;
        }

        final PlayersData playerSetup = plugin.getPlayerSetup();

        try {
            while (result.next()) {
                final ResultSetMetaData metaData = result.getMetaData();
                final UUID uuid = UUID.fromString( result.getString("PlayerUUID") );
                final long minerExp = result.getLong("MinerExp");
                final int minerLvl = result.getInt("MinerLvl");

                final PlayerStats stats = playerSetup.getPlayerStoneMachineStats(uuid);
                stats.setMinerExp(minerExp);
                stats.setMinerLvl(minerLvl);

                plugin.getLogger().log(Level.INFO, "Loading drop configuration for " + result.getString("PlayerUUID"));

                final int columnCount = metaData.getColumnCount();
                for(int i=1; i<=columnCount; i++) {
                    final String columnName = metaData.getColumnName(i);
                    if(columnName.contentEquals("PlayerUUID") || columnName.contentEquals("PlayerName"))
                        continue;
                    else if(columnName.contentEquals("MinerExp") || columnName.contentEquals("MinerLvl"))
                        continue;

                    stats.setStatistic(columnName, result.getInt(columnName));
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Couldn't query results!");
            e.printStackTrace();
        }

    }

    public void loadPersonalDropConfigFromDatabase() {
        final String databaseName = plugin.getDatabaseController().getDatabaseName();
        final String queryStatement = "SELECT * FROM " +databaseName+ ".`" + SQLManager.TABLE_PLAYER_DROP_CONFIG + "`";

        ResultSet result = null;

        try {
            result = plugin.getDatabaseController().runQuery(queryStatement);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if(result == null) {
            plugin.getLogger().log(Level.SEVERE, "Couldn't load Personal Drop Config on start!");
            return;
        }

        final PlayersData playerSetup = plugin.getPlayerSetup();

        try {
            while (result.next()) {
                final ResultSetMetaData metaData = result.getMetaData();
                final UUID uuid = UUID.fromString( result.getString("PlayerUUID") );
                final PlayerConfig config = playerSetup.getPersonalDropConfig(uuid);

                plugin.getLogger().log(Level.INFO, "Loading drop configuration for " + result.getString("PlayerUUID"));

                final int columnCount = metaData.getColumnCount();
                for(int i=1; i<=columnCount; i++) {
                    final String columnName = metaData.getColumnName(i);
                    if(columnName.contentEquals("PlayerUUID") || columnName.contentEquals("PlayerName"))
                        continue;

                    config.setDropEntry(columnName, result.getBoolean(columnName));

                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Couldn't query results!");
            e.printStackTrace();
        }

    }

    public void savePersonalDropConfigInDatabase(PlayerConfig config) {
        try {
            plugin.getDatabaseController().runUpdateForPersonalDropConfig(config);
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Unable to update PersonalDropConfig in database!");
            e.printStackTrace();
        }

        config.onDatabaseSave();
    }

    public void savePersonalStoneStatsInDatabase(PlayerStats stats) {
        try {
            plugin.getDatabaseController().runUpdateForPersonalStoneStats(stats);
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Unable to update PersonalStoneStats in database!");
            e.printStackTrace();
        }

        stats.onDatabaseSave();
    }

    public void saveAllUnsavedDropData() {
        plugin.getLogger().log(Level.INFO, "saveAllUnsavedDropData()");

        int saved = 0, skipped = 0;
        for(PlayerConfig config : playerPersonalDropConfig.values()) {
            if(config.hasUnsavedEdits()) {
                savePersonalDropConfigInDatabase(config);
                saved++;

                continue;
            }

            skipped++;
        }

        plugin.getLogger().log(Level.INFO, "Saved "+saved+" personal configs (skipped: "+skipped+")");

        saved = 0; skipped = 0;
        for(PlayerStats playerStats : stoneMachinePlayerStats.values()) {
            if(playerStats.hasUnsavedEdits()) {
                savePersonalStoneStatsInDatabase(playerStats);
                saved++;

                continue;
            }

            skipped++;
        }

        plugin.getLogger().log(Level.INFO, "Saved "+saved+" player stats (skipped: "+skipped+")");
    }

    public void onDisable() {
        plugin.getLogger().log(Level.INFO, "PlayerSetupManager#onDisable()");
        saveAllUnsavedDropData();
    }

    private PlayerConfig createPersonalDropConfig(UUID uuid) {
        final String playerName = Bukkit.getOfflinePlayer(uuid).getName();
        final PlayerConfig config = new PlayerConfig(uuid, playerName);
        playerPersonalDropConfig.put(uuid, config);

        return playerPersonalDropConfig.get(uuid);
    }
}
