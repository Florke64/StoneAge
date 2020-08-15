/*
 * Copyright Go2Play.pl (c) 2020.
 * Program made for Go2Play Skyblock server. It's not allowed to re-distribute the code.
 * Author: FlrQue
 */

package win.flrque.g2p.stoneage.config;

import org.bukkit.configuration.ConfigurationSection;
import win.flrque.g2p.stoneage.StoneAge;

public abstract class ConfigSectionReader {

    protected final StoneAge plugin;

    protected final ConfigurationSection rootSection;

    public ConfigSectionReader(ConfigurationSection configurationSection) {
        plugin = StoneAge.getPlugin(StoneAge.class);

        rootSection = configurationSection;
    }

}
