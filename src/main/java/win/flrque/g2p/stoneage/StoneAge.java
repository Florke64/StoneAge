package win.flrque.g2p.stoneage;

import org.bukkit.plugin.java.JavaPlugin;
import win.flrque.g2p.stoneage.listener.DebugGameJoin;

public final class StoneAge extends JavaPlugin {

    @Override
    public void onEnable() {
        // Plugin startup logic
        getServer().getPluginManager().registerEvents(new DebugGameJoin(), this);

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
