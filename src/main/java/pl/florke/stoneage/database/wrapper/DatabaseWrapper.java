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

package pl.florke.stoneage.database.wrapper;

import pl.florke.stoneage.database.playerdata.PlayerConfig;
import pl.florke.stoneage.database.playerdata.PlayerStats;
import pl.florke.stoneage.drop.DropMultiplier;

import java.util.UUID;

public interface DatabaseWrapper {

    /**
     * Tries to load player personal drop configuration from database.
     * @return number of loaded configurations.
     */
    int loadPersonalDropConfigFromDatabase();

    /**
     * Executes an update operation for the specified player's drop configuration in the database.
     *
     * @param config the PlayerConfig containing the player's drop settings to be updated
     * @return an integer representing the result of the update operation, indicating success or failure
     */
    int runUpdateForPersonalDropConfig(PlayerConfig config);

    /**
     * Loads all player stone machine statistics from the database.
     * @return the number of loaded statistics.
     */
    int loadPersonalStoneStatsFromDatabase();

    /**
     * Executes an update operation for the specified player's stone statistics in the database.
     *
     * @param stats the PlayerStats containing the player's stone statistics to be updated
     * @return an integer representing the result of the update operation, indicating success or failure
     */
    int runUpdateForPersonalStoneStats(PlayerStats stats);

    /**
     * Inserts a new record into the database to store a drop multiplier.
     *
     * @param callerName the name of the player who set the multiplier
     * @param callerUUID the UUID of the player who set the multiplier
     * @param value the multiplier value
     * @param startMillis the timestamp when the multiplier was set
     * @param timeoutTime the timestamp when the multiplier will expire
     */
    void insertDropMultiplierRecord(String callerName, UUID callerUUID, float value, long startMillis, long timeoutTime);

    /**
     * Retrieves the most recent drop multiplier record from the database
     * and passes it to the provided DropMultiplier object.
     * Used to recover after server reboot.
     *
     * @param multiplier the DropMultiplier object to receive the loaded data
     */
    void readPreviousMultiplierFromDatabase(final DropMultiplier multiplier);

    /**
     * Performs any necessary cleanup before the plugin is disabled.
     * This method is called when the plugin is about to be disabled.
     * It is the responsibility of the implementing class to make sure
     * that any resources allocated by the plugin are cleaned up.
     */
    void onDisable();
}
