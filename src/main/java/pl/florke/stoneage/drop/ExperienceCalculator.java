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
    private final List<Long> experienceTable = new LinkedList<>();
    private int maximumMinerLevel = 99;

    public ExperienceCalculator() {
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
            return experienceTable.getLast();

        return experienceTable.get(level - 2);
    }

    private void initExperienceTableValues() {
        experienceTable.clear(); // clear cache

        final int maxLevel = getMaximumMinerLevel();

        for (int lvl=1; lvl <= (maxLevel); lvl++) {
            double nextLevelFormula = getNextLevelFormula(lvl);

            experienceTable.add((long) nextLevelFormula);
        }

        new Message("ExperienceTable: Generated new Experience Table ($_1 level(s)) from the math formula.")
            .placeholder(1, Integer.toString(experienceTable.size()))
            .log(Level.INFO);
    }

    private double getNextLevelFormula(int level) {
        double nextLevelFormula = (double) INITIAL_XP;
        if (level == 1)
            nextLevelFormula += (double) INITIAL_XP;
        else
            nextLevelFormula += (double) experienceTable.getLast();

        nextLevelFormula += ((double) INITIAL_XP * Math.floor((double) level / 10d) * Math.pow(2, 0));
        nextLevelFormula += ((double) INITIAL_XP * Math.floor((double) level / 50d) * Math.pow(2, 16));
        nextLevelFormula += ((double) INITIAL_XP * Math.floor((double) level / 100d) * Math.pow(2, 16)) * 2;

        new Message("Level " + level + " - Exp needed: " + nextLevelFormula);

        return nextLevelFormula;
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
