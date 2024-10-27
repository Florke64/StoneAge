/*
 * @Florke64 <Daniel Chojnacki>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package pl.florke.stoneage.database.playerdata;

import org.bukkit.Bukkit;
import pl.florke.stoneage.StoneAge;
import pl.florke.stoneage.util.Message;

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

    /**
     * Retrieves the {@link PlayerStats} instance for the given player UUID.
     * If an instance doesn't exist yet, it will be created and stored in the cache.
     * @param uuid the UUID of the player
     * @return the retrieved {@link PlayerStats} instance
     */
    public PlayerStats getPlayerStoneMachineStats(UUID uuid) {
        if (!stoneMachinePlayerStats.containsKey(uuid))
            stoneMachinePlayerStats.put(uuid, createStoneMachinePlayerStats(uuid));

        return stoneMachinePlayerStats.get(uuid);
    }

    /**
     * Creates a new {@link PlayerStats} instance for the given player UUID and stores it in the cache.
     * This method is used by {@link #getPlayerStoneMachineStats(UUID)} when an instance doesn't exist yet.
     * @param uuid the UUID of the player
     * @return the created {@link PlayerStats} instance
     */
    private PlayerStats createStoneMachinePlayerStats(UUID uuid) {
        final String playerName = Bukkit.getOfflinePlayer(uuid).getName();
        final PlayerStats stats = new PlayerStats(uuid, playerName);
        stoneMachinePlayerStats.put(uuid, stats);

        return stoneMachinePlayerStats.get(uuid);
    }

    /**
     * Retrieves the {@link PlayerConfig} instance for the given player UUID.
     * If an instance doesn't exist yet, it will be created and stored in the cache.
     * @param uuid the UUID of the player
     * @return the retrieved or newly created {@link PlayerConfig} instance
     */
    public PlayerConfig getPersonalDropConfig(UUID uuid) {
        if (!playerPersonalDropConfig.containsKey(uuid))
            return createPersonalDropConfig(uuid);

        return playerPersonalDropConfig.get(uuid);
    }

    /**
     * Creates a new {@link PlayerConfig} instance for the given player UUID and stores it in the cache.
     * This method is used by {@link #getPersonalDropConfig(UUID)} when an instance doesn't exist yet.
     * @param uuid the UUID of the player
     * @return the created {@link PlayerConfig} instance
     */
    private PlayerConfig createPersonalDropConfig(UUID uuid) {
        final String playerName = Bukkit.getOfflinePlayer(uuid).getName();
        final PlayerConfig config = new PlayerConfig(uuid, playerName);
        playerPersonalDropConfig.put(uuid, config);

        return playerPersonalDropConfig.get(uuid);
    }

    public int savePersonalDropConfigInDatabase(PlayerConfig config) {
        final int response = plugin.getSQLWrapper().runUpdateForPersonalDropConfig(config);

        if (response > 0)
            config.onDatabaseSave();

        return response;
    }

    public int savePersonalStoneStatsInDatabase(PlayerStats stats) {
        final int response = plugin.getSQLWrapper().runUpdateForPersonalStoneStats(stats);

        if (response > 0)
            stats.onDatabaseSave();

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

        new Message("Saved $_1 personal configs (skipped: $_2)")
                .placeholder(1, String.valueOf(saved))
                .placeholder(2, String.valueOf(skipped))
                .log(Level.INFO);

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

        new Message("Saved " + saved + " player stats (skipped: " + skipped + ")")
                .placeholder(1, String.valueOf(saved))
                .placeholder(2, String.valueOf(skipped))
                .log(Level.INFO);
    }

    public void onDisable() {
        saveAllUnsavedDropData();
    }
}
