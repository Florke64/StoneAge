/*
 * Copyright Go2Play.pl (c) 2020.
 * Program made for Go2Play Skyblock server. It's not allowed to re-distribute the code.
 * Author: FlrQue
 */

package win.flrque.g2p.stoneage.database.playerdata;

import org.bukkit.Bukkit;
import win.flrque.g2p.stoneage.StoneAge;
import win.flrque.g2p.stoneage.database.SQLManager;

import java.sql.*;
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
        if (!stoneMachinePlayerStats.containsKey(uuid)) {
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
        if (!playerPersonalDropConfig.containsKey(uuid)) {
            createPersonalDropConfig(uuid);
        }

        return playerPersonalDropConfig.get(uuid);
    }

    public int loadPersonalStoneStatsFromDatabase() {
        final String databaseName = plugin.getDatabaseController().getDatabaseName();
        final String queryStatement = "SELECT * FROM " + databaseName + ".`" + SQLManager.TABLE_PLAYER_STATS + "`";

        final PlayersData playerSetup = plugin.getPlayersData();

        try (final Connection conn = plugin.getDatabaseController().getConnection();
             final PreparedStatement ps = conn.prepareStatement(queryStatement);
             final ResultSet result = ps.executeQuery()) {

            if (result == null) {
                plugin.getLogger().log(Level.SEVERE, "Couldn't load Personal Stone Stats on start!");
                return -1;
            }

            int loadCount = 0;
            while (result.next()) {
                final ResultSetMetaData metaData = result.getMetaData();
                final UUID uuid = UUID.fromString(result.getString("PlayerUUID"));
                final long minerExp = result.getLong("MinerExp");
                final int minerLvl = result.getInt("MinerLvl");

                final PlayerStats stats = playerSetup.getPlayerStoneMachineStats(uuid);
                stats.setMinerExp(minerExp, false);
                stats.setMinerLvl(minerLvl, false);

                //plugin.getLogger().log(Level.INFO, "Loading drop stats for " + result.getString("PlayerUUID"));

                final int columnCount = metaData.getColumnCount();
                for (int i = 1; i <= columnCount; i++) {
                    final String columnName = metaData.getColumnName(i);
                    if (columnName.contentEquals("PlayerUUID") || columnName.contentEquals("PlayerName"))
                        continue;
                    else if (columnName.contentEquals("MinerExp") || columnName.contentEquals("MinerLvl"))
                        continue;

                    stats.setStatistic(columnName, result.getInt(columnName));
                }

                stats.markUnsaved(false);

                loadCount++;
            }

            return loadCount;
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, "Couldn't query results!");
            ex.printStackTrace();
        }

        return 0;
    }

    public int loadPersonalDropConfigFromDatabase() {
        final String databaseName = plugin.getDatabaseController().getDatabaseName();
        final String queryStatement = "SELECT * FROM " + databaseName + ".`" + SQLManager.TABLE_PLAYER_DROP_CONFIG + "`";

        final PlayersData playerSetup = plugin.getPlayersData();

        try (final Connection conn = plugin.getDatabaseController().getConnection();
             final PreparedStatement ps = conn.prepareStatement(queryStatement);
             final ResultSet result = ps.executeQuery()) {

            if (result == null) {
                plugin.getLogger().log(Level.SEVERE, "Couldn't load Personal Stone Stats on start!");
                return -1;
            }

            int loadCount = 0;
            while (result.next()) {
                final ResultSetMetaData metaData = result.getMetaData();
                final UUID uuid = UUID.fromString(result.getString("PlayerUUID"));
                final PlayerConfig config = playerSetup.getPersonalDropConfig(uuid);

                //plugin.getLogger().log(Level.INFO, "Loading drop configuration for " + result.getString("PlayerUUID"));

                final int columnCount = metaData.getColumnCount();
                for (int i = 1; i <= columnCount; i++) {
                    final String columnName = metaData.getColumnName(i);
                    if (columnName.contentEquals("PlayerUUID") || columnName.contentEquals("PlayerName"))
                        continue;

                    config.setDropEntry(columnName, result.getBoolean(columnName));

                }

                config.markUnsaved(false);

                loadCount++;
            }

            return loadCount;
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Couldn't query results!");
            e.printStackTrace();
        }

        return 0;
    }

    public int savePersonalDropConfigInDatabase(PlayerConfig config) {
        final int response = plugin.getDatabaseController().runUpdateForPersonalDropConfig(config);

        if (response > 0) {
            config.onDatabaseSave();
        }

        return response;
    }

    public int savePersonalStoneStatsInDatabase(PlayerStats stats) {
        final int response = plugin.getDatabaseController().runUpdateForPersonalStoneStats(stats);

        if (response > 0) {
            stats.onDatabaseSave();
        }

        return response;
    }

    public void saveAllUnsavedDropData() {
        int saved = 0, skipped = 0;
        for (PlayerConfig config : playerPersonalDropConfig.values()) {
            if (config.hasUnsavedEdits()) {
                savePersonalDropConfigInDatabase(config);
                saved++;

                continue;
            }

            skipped++;
        }

        plugin.getLogger().log(Level.INFO, "Saved " + saved + " personal configs (skipped: " + skipped + ")");

        saved = 0;
        skipped = 0;
        for (PlayerStats playerStats : stoneMachinePlayerStats.values()) {
            if (playerStats.hasUnsavedEdits()) {
                savePersonalStoneStatsInDatabase(playerStats);
                saved++;

                continue;
            }

            skipped++;
        }

        plugin.getLogger().log(Level.INFO, "Saved " + saved + " player stats (skipped: " + skipped + ")");
    }

    public void onDisable() {
        saveAllUnsavedDropData();
    }

    private PlayerConfig createPersonalDropConfig(UUID uuid) {
        final String playerName = Bukkit.getOfflinePlayer(uuid).getName();
        final PlayerConfig config = new PlayerConfig(uuid, playerName);
        playerPersonalDropConfig.put(uuid, config);

        return playerPersonalDropConfig.get(uuid);
    }
}
