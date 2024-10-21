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

package pl.florke.stoneage.drop;

import pl.florke.stoneage.StoneAge;
import pl.florke.stoneage.util.Message;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;

public class ExperienceCalculator {

    public final long INITIAL_XP = 500;
    private final StoneAge plugin;
    private final List<Long> experienceTable = new LinkedList<>();
    private int maximumMinerLevel = 99;

    public ExperienceCalculator() {
        this.plugin = StoneAge.getPlugin(StoneAge.class);

        initExperienceTableValues();
    }

    public int expToLevel(final long experience) {
        int lvl = 1;
        for (final Long requiredExp : this.experienceTable) {
            if (requiredExp > experience)
                break;
            else lvl++;
        }

        return lvl;
    }

    public long getExpNeededToLevel(final int level) {
        if (level < 2) return 0;
        else if (level > getMaximumMinerLevel())
            return experienceTable.get(experienceTable.size());

        return experienceTable.get(level - 2);
    }

    private void initExperienceTableValues() {
        final int start_x = 1;
        final int maxLevel = getMaximumMinerLevel();

        experienceTable.clear();

        for (int x = start_x; x <= (maxLevel); x++) {
            double nextLevelFormula = (double) INITIAL_XP;
            if (x == start_x) {
                nextLevelFormula += (double) INITIAL_XP;
            } else {
                nextLevelFormula += (double) experienceTable.get(experienceTable.size() - 1);
            }
            nextLevelFormula += ((double) INITIAL_XP * Math.floor((double) x / 10d) * Math.pow(2, 0));
            nextLevelFormula += ((double) INITIAL_XP * Math.floor((double) x / 50d) * Math.pow(2, 16));
            nextLevelFormula += ((double) INITIAL_XP * Math.floor((double) x / 100d) * Math.pow(2, 16)) * 2;

            experienceTable.add((long) nextLevelFormula);
        }

        final Message success = new Message("ExperienceTable: Generated new Experience Table ($_1 level(s)) from the math formula.");
        success.setVariable(1, Integer.toString(experienceTable.size()));
        plugin.getLogger().log(Level.INFO, success.getPreparedMessage().get(0));

//        int level = 2;
//        for(final long exp : experienceTable) {
//            plugin.getLogger().log(Level.INFO, "Level " + level + " - Exp needed: " + exp);
//            level++;
//        }
    }

    public int getMaximumMinerLevel() {
        return this.maximumMinerLevel;
    }

    public void setMaximumMinerLevel(int maximumMinerLevel) {
        if (maximumMinerLevel != this.maximumMinerLevel) {
            this.maximumMinerLevel = maximumMinerLevel;
            initExperienceTableValues();
        }
    }

}
