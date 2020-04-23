package win.flrque.g2p.stoneage;

import org.bukkit.plugin.java.JavaPlugin;
import win.flrque.g2p.stoneage.item.StoneMachine;
import win.flrque.g2p.stoneage.listener.DebugGameJoin;
import win.flrque.g2p.stoneage.listener.StoneMachinePlaceListener;

import java.util.List;

public final class StoneAge extends JavaPlugin {

    private StoneMachine stoneMachine;

    @Override
    public void onEnable() {
        // Plugin startup logic

        initStoneMachines();

        getServer().getPluginManager().registerEvents(new StoneMachinePlaceListener(), this);
        getServer().getPluginManager().registerEvents(new DebugGameJoin(), this);

    }

    private void initStoneMachines() {
        final List<String> machineLore = StoneMachine.createDefaultMachineLore();

        //TODO: replace with config values
        stoneMachine = new StoneMachine("&6&lStoniarka", machineLore);
    }

    public StoneMachine getStoneMachine() {
        return this.stoneMachine;
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
