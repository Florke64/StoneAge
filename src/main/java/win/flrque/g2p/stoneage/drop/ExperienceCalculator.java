/*
 * Copyright Go2Play.pl (c) 2020.
 * Program made for Go2Play Skyblock server. It's not allowed to re-distribute the code.
 * Author: FlrQue
 */

package win.flrque.g2p.stoneage.drop;

import win.flrque.g2p.stoneage.StoneAge;

import java.util.LinkedList;
import java.util.List;

public class ExperienceCalculator {

    private final StoneAge plugin;

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
        experienceTable.add(300L);
        experienceTable.add(800L);
        experienceTable.add(1_500L);
        experienceTable.add(2_500L);
        experienceTable.add(4_300L);
        experienceTable.add(7_200L);
        experienceTable.add(1_1000L);
        experienceTable.add(17_000L);
        experienceTable.add(24_000L);
        experienceTable.add(33_000L);
        experienceTable.add(43_000L);
        experienceTable.add(58_000L);
        experienceTable.add(76_000L);
        experienceTable.add(100_000L);
        experienceTable.add(130_000L);
        experienceTable.add(169_000L);
        experienceTable.add(219_000L);
        experienceTable.add(283_000L);
        experienceTable.add(365_000L);
        experienceTable.add(472_000L);
        experienceTable.add(610_000L);
        experienceTable.add(705_000L);
        experienceTable.add(813_000L);
        experienceTable.add(937_000L);
        experienceTable.add(1_077_000L);
        experienceTable.add(1_237_000L);
        experienceTable.add(1_418_000L);
        experienceTable.add(1_624_000L);
        experienceTable.add(1_857_000L);
        experienceTable.add(2_122_000L);
        experienceTable.add(2_421_000L);
        experienceTable.add(2_761_000L);
        experienceTable.add(3_145_000L);
        experienceTable.add(3_580_000L);
        experienceTable.add(4_073_000L);
        experienceTable.add(4_632_000L);
        experienceTable.add(5_194_000L);
        experienceTable.add(5_717_000L);
        experienceTable.add(6_264_000L);
        experienceTable.add(6_837_000L);
        experienceTable.add(7_600_000L);
        experienceTable.add(8_274_000L);
        experienceTable.add(8_990_000L);
        experienceTable.add(9_753_000L);
        experienceTable.add(10_560_000L);
        experienceTable.add(11_410_000L);
        experienceTable.add(12_320_000L);
        experienceTable.add(13_270_000L);
        experienceTable.add(14_280_000L);
        experienceTable.add(15_340_000L);
        experienceTable.add(16_870_000L);
        experienceTable.add(18_960_000L);
        experienceTable.add(19_980_000L);
        experienceTable.add(21_420_000L);
        experienceTable.add(22_930_000L);
        experienceTable.add(24_580_000L);
        experienceTable.add(26_200_000L);
        experienceTable.add(27_960_000L);
        experienceTable.add(29_800_000L);
        experienceTable.add(32_780_000L);
        experienceTable.add(36_060_000L);
        experienceTable.add(39_670_000L);
        experienceTable.add(43_640_000L);
        experienceTable.add(48_000_000L);
        experienceTable.add(52_800_000L);
        experienceTable.add(58_080_000L);
        experienceTable.add(63_890_000L);
        experienceTable.add(70_280_000L);
        experienceTable.add(77_310_000L);
        experienceTable.add(85_040_000L);
        experienceTable.add(93_540_000L);
        experienceTable.add(102_900_000L);
        experienceTable.add(113_200_000L);
        experienceTable.add(124_500_000L);
        experienceTable.add(137_000_000L);
        experienceTable.add(150_700_000L);
        experienceTable.add(165_700_000L);
        experienceTable.add(236_990_000L);
        experienceTable.add(260_650_000L);
        experienceTable.add(286_780_000L);
        experienceTable.add(315_000_000L);
        experienceTable.add(346_970_000L);
        experienceTable.add(381_680_000L);
        experienceTable.add(419_770_000L);
        experienceTable.add(461_760_000L);
        experienceTable.add(508_040_000L);
        experienceTable.add(558_740_000L);
        experienceTable.add(614_640_000L);
        experienceTable.add(676_130_000L);
        experienceTable.add(743_730_000L);
        experienceTable.add(1_041_222_000L);
        experienceTable.add(1_145_344_200L);
        experienceTable.add(1_259_878_620L);
        experienceTable.add(1_385_866_482L);
        experienceTable.add(1_524_453_130L);
        experienceTable.add(1_676_898_443L);
        experienceTable.add(1_844_588_288L);
        experienceTable.add(2_029_047_116L);
        experienceTable.add(2_050_000_000L);
        experienceTable.add(2_150_000_000L);
    }

}
