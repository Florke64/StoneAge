/*
 * Copyright Go2Play.pl (c) 2020.
 * Program made for Go2Play Skyblock server. It's not allowed to re-distribute the code.
 * Author: FlrQue
 */

package win.flrque.g2p.stoneage.drop;

import win.flrque.g2p.stoneage.StoneAge;
import win.flrque.g2p.stoneage.util.Message;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;

public class ExperienceCalculator {

    private final StoneAge plugin;

    public final long INITIAL_EXP_NEEDED = 300;

    private int maximumMinerLevel = 99;

    private final List<Long> experienceTable = new LinkedList<>();

    public ExperienceCalculator() {
        this.plugin = StoneAge.getPlugin(StoneAge.class);

        initExperienceTableValues();
    }

    public int expToLevel(final long experience) {
        int lvl = 1;
        for(final Long requiredExp : this.experienceTable) {
            if(requiredExp > experience)
                break;
            else lvl ++;
        }

        return lvl;
    }

    //TODO: Make config file ?
    private void initExperienceTableValues() {
        final int start_i = 1;
        final int maxLevel = getMaximumMinerLevel();

        experienceTable.clear();

        for(int i = start_i; i <= (maxLevel); i++) {
            if(i == start_i) {
                experienceTable.add(INITIAL_EXP_NEEDED);
            } else {
                final long previousLevelExp = experienceTable.get(i-start_i-1);
                final double nextLevelGap = Math.ceil( (previousLevelExp*0.12d)+(previousLevelExp/(i-1)) );

                experienceTable.add(previousLevelExp + (long) nextLevelGap);
            }
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

    public void setMaximumMinerLevel(int maximumMinerLevel) {
        if(maximumMinerLevel != this.maximumMinerLevel) {
            this.maximumMinerLevel = maximumMinerLevel;
            initExperienceTableValues();
        }
    }

    public int getMaximumMinerLevel() {
        return this.maximumMinerLevel;
    }

}
