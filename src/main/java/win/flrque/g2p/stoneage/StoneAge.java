package win.flrque.g2p.stoneage;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import win.flrque.g2p.stoneage.command.DropCommand;
import win.flrque.g2p.stoneage.drop.DropCalculator;
import win.flrque.g2p.stoneage.drop.DropEntry;
import win.flrque.g2p.stoneage.gui.WindowManager;
import win.flrque.g2p.stoneage.listener.*;
import win.flrque.g2p.stoneage.machine.StoneMachine;
import win.flrque.g2p.stoneage.util.ConfigSectionDropEntry;
import win.flrque.g2p.stoneage.util.ConfigSectionGeneral;

import java.util.List;
import java.util.logging.Level;

public final class StoneAge extends JavaPlugin {

    private StoneMachine stoneMachine;

    private WindowManager windowManager;
    private DropCalculator dropCalculator;

    @Override
    public void onEnable() {
        // Plugin startup logic

        //Setting-up Stone Generator machines
        dropCalculator = new DropCalculator();
        dropCalculator.addDrop(new DropEntry(new ItemStack(Material.DIAMOND), 1.0f));

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
        getLogger().log(Level.INFO, "Reloading configuration file...");
        super.reloadConfig();

        //Reading 'General' configuration for Stone Machines
        if(!getConfig().isConfigurationSection("machines")) {
            getLogger().log(Level.SEVERE, "Invalid Configuration file (missing \"machines\" section)!");
            getLogger().log(Level.SEVERE, this.getName() + " plugin will now be disabled.");
            getPluginLoader().disablePlugin(this);
            return;
        }

        final ConfigSectionGeneral generalConfig = new ConfigSectionGeneral(getConfig().getConfigurationSection("machines"));
        //TODO: Apply general config fully

        //Reading Primitive Stone drop
        if(!getConfig().isConfigurationSection("primitive_drop")) {
            getLogger().log(Level.SEVERE, "Invalid Configuration file (missing \"primitive_drop\" section)!");
            getLogger().log(Level.SEVERE, this.getName() + " plugin will now be disabled.");
            getPluginLoader().disablePlugin(this);
            return;
        }

        final ConfigSectionDropEntry primitiveDropEntry = new ConfigSectionDropEntry(getConfig().getConfigurationSection("primitive_drop"));
        dropCalculator.setPrimitiveDrop(primitiveDropEntry.compileDropEntry());

        //Reading Custom Stone drop
        if(!getConfig().isConfigurationSection("custom_drops")) {
            getLogger().log(Level.SEVERE, "Invalid Configuration file (missing \"custom_drops\" section)!");
            getLogger().log(Level.SEVERE, "Skipping, stone will drop server-default items.");
            return;
        }

        int customDropsCount = 0;
        final ConfigurationSection customDropsSection = getConfig().getConfigurationSection("custom_drops");
        for(String entryName : customDropsSection.getKeys(false)) {
            final ConfigSectionDropEntry customDropEntry = new ConfigSectionDropEntry(customDropsSection.getConfigurationSection(entryName));
            dropCalculator.addDrop(customDropEntry.compileDropEntry());

            getLogger().log(Level.INFO, "Loaded custom drop: "+ entryName);

            customDropsCount ++;
        }

        getLogger().log(Level.FINE, "Config reloaded!");
        getLogger().log(Level.INFO, "Loaded "+ customDropsCount +" custom drop entries.");
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
