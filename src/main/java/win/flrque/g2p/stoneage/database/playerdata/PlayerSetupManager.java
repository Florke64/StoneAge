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

public class PlayerSetupManager {

    private final StoneAge plugin;

    private final Map<UUID, PersonalDropConfig> playerPersonalDropConfig = new HashMap<>();
    private final Map<UUID, StoneMachinePlayerStats> stoneMachinePlayerStats = new HashMap<>();

    public PlayerSetupManager() {
        plugin = StoneAge.getPlugin(StoneAge.class);
    }

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

    public void loadPersonalStoneStatsFromDatabase() {
        final String databaseName = plugin.getDatabaseController().getDatabaseName();
        final String queryStatement = "SELECT * FROM " +databaseName+ ".`" + SQLManager.TABLE_PLAYER_STATS + "`";

        ResultSet result = null;

        try {
            plugin.getDatabaseController().runSelectQuery(queryStatement);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if(result == null) {
            plugin.getLogger().log(Level.SEVERE, "Couldn't load Personal Stone Stats on start!");
            return;
        }

        final PlayerSetupManager playerSetup = plugin.getPlayerSetup();

        try {
            while (result.next()) {
                final ResultSetMetaData metaData = result.getMetaData();
                final UUID uuid = UUID.fromString( result.getString("PlayerUUID") );
                final StoneMachinePlayerStats stats = playerSetup.getPlayerStoneMachineStats(uuid);

                final int columnCount = metaData.getColumnCount();
                for(int i=0; i<columnCount; i++) {
                    final String columnName = metaData.getColumnName(i);
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
            plugin.getDatabaseController().runSelectQuery(queryStatement);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if(result == null) {
            plugin.getLogger().log(Level.SEVERE, "Couldn't load Personal Stone Stats on start!");
            return;
        }

        final PlayerSetupManager playerSetup = plugin.getPlayerSetup();

        try {
            while (result.next()) {
                final ResultSetMetaData metaData = result.getMetaData();
                final UUID uuid = UUID.fromString( result.getString("PlayerUUID") );
                final PersonalDropConfig config = playerSetup.getPersonalDropConfig(uuid);

                final int columnCount = metaData.getColumnCount();
                for(int i=0; i<columnCount; i++) {
                    final String columnName = metaData.getColumnName(i);
                    config.setDropEntry(columnName, result.getBoolean(columnName));
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Couldn't query results!");
            e.printStackTrace();
        }

    }

    private PersonalDropConfig createPersonalDropConfig(UUID uuid) {
        final String playerName = Bukkit.getOfflinePlayer(uuid).getName();
        final PersonalDropConfig config = new PersonalDropConfig(uuid, playerName);
        playerPersonalDropConfig.put(uuid, config);

        return playerPersonalDropConfig.get(uuid);
    }

}
