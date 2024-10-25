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

import pl.florke.stoneage.StoneAge;
import pl.florke.stoneage.drop.DropEntry;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class PlayerConfig {

    private final StoneAge plugin;

    private final UUID uuid;
    private final String playerName;
    private final Map<DropEntry, Boolean> customDropEntries = new HashMap<>();
    private boolean unsavedEdits = false;

    public PlayerConfig(final UUID uuid, final String playerName) {
        plugin = StoneAge.getPlugin(StoneAge.class);

        this.uuid = uuid;
        this.playerName = playerName;

        customDropEntries.put(plugin.getDropCalculator().getPrimitiveDropEntry(), true);
        for (DropEntry drop : plugin.getDropCalculator().getDropEntries()) {
            customDropEntries.put(drop, true);
        }

    }

    public boolean isDropping(DropEntry dropEntry) {
        return customDropEntries.containsKey(dropEntry) && customDropEntries.get(dropEntry);
    }

    public void setDropEntry(String entryKey, boolean shouldDrop) {
        DropEntry dropEntry = plugin.getDropCalculator().getDropEntry(entryKey);
        setDropEntry(dropEntry, shouldDrop);
    }

    public void setDropEntry(DropEntry dropEntry, boolean shouldDrop) {
        customDropEntries.put(dropEntry, shouldDrop);
    }

    public boolean switchDropEntry(DropEntry dropEntry) {
        setDropEntry(dropEntry, !isDropping(dropEntry));
        markUnsaved(true);

        return isDropping(dropEntry);
    }

    public Set<DropEntry> getCustomDropEntries() {
        return customDropEntries.keySet();
    }

    public boolean hasUnsavedEdits() {
        return unsavedEdits;
    }

    public void onDatabaseSave() {
        markUnsaved(false);
    }

    public UUID getUniqueId() {
        return uuid;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void markUnsaved(final boolean hasUnsavedEdits) {
        this.unsavedEdits = hasUnsavedEdits;
    }

}
