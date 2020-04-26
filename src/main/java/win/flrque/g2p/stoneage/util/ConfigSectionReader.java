package win.flrque.g2p.stoneage.util;

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
