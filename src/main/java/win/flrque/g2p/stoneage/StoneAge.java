package win.flrque.g2p.stoneage;

import org.bukkit.plugin.java.JavaPlugin;
import win.flrque.g2p.stoneage.gui.WindowManager;
import win.flrque.g2p.stoneage.listener.*;
import win.flrque.g2p.stoneage.machine.StoneMachine;

import java.util.List;

public final class StoneAge extends JavaPlugin {

    private StoneMachine stoneMachine;

    private WindowManager windowManager;

    @Override
    public void onEnable() {
        // Plugin startup logic

        initStoneMachines();

        getServer().getPluginManager().registerEvents(new StoneMachinePlaceListener(), this);
        getServer().getPluginManager().registerEvents(new StoneMachineBreakListener(), this);
        getServer().getPluginManager().registerEvents(new StoneMachineInteractListener(), this);
        getServer().getPluginManager().registerEvents(new StoneBreakListener(), this);

        getServer().getPluginManager().registerEvents(new WindowClickListener(), this);

        getServer().getPluginManager().registerEvents(new DebugGameJoin(), this);

        windowManager = new WindowManager();

    }

    private void initStoneMachines() {
        final List<String> machineLore = StoneMachine.createDefaultMachineLore();

        //TODO: replace with config values
        stoneMachine = new StoneMachine("&6&lStoniarka", machineLore);
    }

    public StoneMachine getStoneMachine() {
        return this.stoneMachine;
    }

    public WindowManager getWindowManager() {
        return this.windowManager;
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
