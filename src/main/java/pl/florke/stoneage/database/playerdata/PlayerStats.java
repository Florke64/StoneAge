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

import org.bukkit.NamespacedKey;
import pl.florke.stoneage.StoneAge;
import pl.florke.stoneage.drop.DropEntry;
import pl.florke.stoneage.drop.DropEntryManager;
import pl.florke.stoneage.drop.ExperienceCalculator;
import pl.florke.stoneage.event.MinerLevelUpEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class PlayerStats {

    private final StoneAge plugin = StoneAge.getPlugin(StoneAge.class);

    private final UUID uuid;
    private final String playerName;
    private final Map<NamespacedKey, Integer> statistics = new HashMap<>();
    private boolean unsavedEdits = false;
    private long minerExp;
    private int minerLvl;

    public PlayerStats(final UUID uuid, final String playerName) {
        final DropEntryManager dropEntryManager = plugin.getDropCalculator().getDropEntryManager();

        this.uuid = uuid;
        this.playerName = playerName;

        this.minerExp = ExperienceCalculator.INITIAL_XP;
        this.minerLvl = 1;

        for (DropEntry entry : dropEntryManager.getCustomDropEntries())
            statistics.put(entry.getKey(), 0);

        for (DropEntry entry : dropEntryManager.getDropResourcesEntries().values())
            statistics.put(entry.getKey(), 0);
    }

    public void setStatistic(NamespacedKey key, int value) {
        statistics.put(key, value);
    }

    @SuppressWarnings("unused")
    public int increaseStatistic(NamespacedKey key) {
        return increaseStatistic(key, 1);
    }

    public int increaseStatistic(NamespacedKey key, int amount) {
        final int value = getStatistic(key) + amount;
        statistics.put(key, value);
        markUnsaved(true);

        return value;
    }

    public int getStatistic(NamespacedKey key) {
        return statistics.getOrDefault(key, 0);
    }

    public Set<NamespacedKey> getStatisticKeys() {
        return statistics.keySet();
    }

    public String getPlayerName() {
        return playerName;
    }

    public UUID getUniqueId() {
        return uuid;
    }

    public boolean hasUnsavedEdits() {
        return unsavedEdits;
    }

    public void onDatabaseSave() {
        markUnsaved(false);
    }

    public void addMinerExp(final long expAmount) {
        setMinerExp(this.minerExp + expAmount, true);
    }

    public void setMinerExp(final long minerExp, final boolean updateLevel) {
        final int updatedLevel = plugin.getDropCalculator().getExpCalculator().expToLevel(minerExp);

        if (updateLevel && updatedLevel > this.minerLvl) {
            setMinerLvl(updatedLevel, true);
        }

        this.minerExp = minerExp;

        markUnsaved(true);
    }

    public long getMinerExp() {
        return this.minerExp;
    }

    public void setMinerLvl(final int minerLvl, final boolean callEvent) {
        if (callEvent) {
            final MinerLevelUpEvent event = new MinerLevelUpEvent(this, minerLvl);
            this.plugin.getServer().getPluginManager().callEvent(event);

            if (event.isCancelled())
                return;
        }

        this.minerLvl = minerLvl;

        markUnsaved(true);
    }

    public int getMinerLvl() {
        return minerLvl;
    }

    public void markUnsaved(final boolean hasUnsavedEdits) {
        this.unsavedEdits = hasUnsavedEdits;
    }

}
