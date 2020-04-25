package win.flrque.g2p.stoneage;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import win.flrque.g2p.stoneage.command.DropCommand;
import win.flrque.g2p.stoneage.drop.DropCalculator;
import win.flrque.g2p.stoneage.gui.WindowManager;
import win.flrque.g2p.stoneage.listener.*;
import win.flrque.g2p.stoneage.machine.StoneMachine;

import java.util.List;

public final class StoneAge extends JavaPlugin {

    private StoneMachine stoneMachine;

    private WindowManager windowManager;
    private DropCalculator dropCalculator;

    @Override
    public void onEnable() {
        // Plugin startup logic

        //Setting-up Stone Generator machines
        dropCalculator = new DropCalculator();
        initStoneMachines();

        //Saving and reloading config
        saveDefaultConfig();
        reloadConfig();

        //Registering Event Listeners for the Plugin
        getServer().getPluginManager().registerEvents(new StoneMachinePlaceListener(), this);
        getServer().getPluginManager().registerEvents(new StoneMachineBreakListener(), this);
        getServer().getPluginManager().registerEvents(new StoneMachineInteractListener(), this);
        getServer().getPluginManager().registerEvents(new StoneBreakListener(), this);

        getServer().getPluginManager().registerEvents(new WindowClickListener(), this);

        getServer().getPluginManager().registerEvents(new DebugGameJoin(), this);

        //Registering Plugin Commands
        getCommand("drop").setExecutor(new DropCommand());

        windowManager = new WindowManager();

    }

    private void initStoneMachines() {
        final List<String> machineLore = StoneMachine.createDefaultMachineLore();

        //TODO: replace with config values
        stoneMachine = new StoneMachine("&6&lStoniarka", machineLore);
    }

    @Override
    public void reloadConfig() {
        super.reloadConfig();

        //TODO: Import configuration settings (items i.e)
    }

    public StoneMachine getStoneMachine() {
        return this.stoneMachine;
    }

    public WindowManager getWindowManager() {
        return this.windowManager;
    }

    public DropCalculator getDropCalculator() {
        return dropCalculator;
    }

    @Override
    public FileConfiguration getConfig() {
        return super.getConfig();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
