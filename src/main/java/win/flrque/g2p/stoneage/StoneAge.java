/*
 * Copyright Go2Play.pl (c) 2020.
 * Program made for Go2Play Skyblock server. It's not allowed to re-distribute the code.
 * Author: FlrQue
 */

package win.flrque.g2p.stoneage;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import win.flrque.g2p.stoneage.command.DropCommand;
import win.flrque.g2p.stoneage.command.DropHelpCommand;
import win.flrque.g2p.stoneage.command.DropMultiplierCommand;
import win.flrque.g2p.stoneage.database.SQLManager;
import win.flrque.g2p.stoneage.database.playerdata.PersonalDropConfig;
import win.flrque.g2p.stoneage.database.playerdata.PlayerSetupManager;
import win.flrque.g2p.stoneage.database.playerdata.StoneMachinePlayerStats;
import win.flrque.g2p.stoneage.drop.DropCalculator;
import win.flrque.g2p.stoneage.drop.DropMultiplier;
import win.flrque.g2p.stoneage.gui.WindowManager;
import win.flrque.g2p.stoneage.listener.*;
import win.flrque.g2p.stoneage.machine.StoneMachine;
import win.flrque.g2p.stoneage.util.ConfigSectionDatabase;
import win.flrque.g2p.stoneage.util.ConfigSectionDropEntry;
import win.flrque.g2p.stoneage.util.ConfigSectionGeneral;

import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

public final class StoneAge extends JavaPlugin {

    private StoneMachine stoneMachine;
    private PlayerSetupManager playerSetup;

    private WindowManager windowManager;
    private DropCalculator dropCalculator;

    private SQLManager sqlManager;
    private BukkitRunnable autosaveRunnable;

    @Override
    public void onEnable() {
        // Plugin startup logic
        windowManager = new WindowManager();
        dropCalculator = new DropCalculator();
        playerSetup = new PlayerSetupManager();

        initStoneMachines();

        //Saving and reloading config
        saveDefaultConfig();
        reloadConfig();

        //Registering Event Listeners for the Plugin
        getServer().getPluginManager().registerEvents(new StoneMachinePlaceListener(), this);
        getServer().getPluginManager().registerEvents(new StoneMachineBreakListener(), this);
        getServer().getPluginManager().registerEvents(new StoneMachineInteractListener(), this);
        getServer().getPluginManager().registerEvents(new StoneMachineHopperInteractListener(), this);
        getServer().getPluginManager().registerEvents(new StoneMachineRedstoneInteractListener(), this);
        getServer().getPluginManager().registerEvents(new StoneBreakListener(), this);

        //TODO: Consider creating PersonalConfig and StoneMachineStats on Player Join?
        getServer().getPluginManager().registerEvents(new PlayerSaveDataOnLeaveListener(), this);
        getServer().getPluginManager().registerEvents(new WindowClickListener(), this);

        getServer().getPluginManager().registerEvents(new StatisticsIncreaseListener(), this);

        getServer().getPluginManager().registerEvents(new DebugGameJoin(), this);

        //Registering Plugin Commands
        getCommand("drop").setExecutor(new DropCommand());
        getCommand("drophelp").setExecutor(new DropHelpCommand());
        getCommand("multiplier").setExecutor(new DropMultiplierCommand());

        final long minute = 60 * 1000;
        final long period = 15;

        initAsyncAutosave(period);
        if(autosaveRunnable != null)
            autosaveRunnable.runTaskTimerAsynchronously(this, period * minute, period * minute);
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
        generalConfig.compile();

        getStoneMachine().setStoneRespawnFrequency(generalConfig.getStoneFrequency());
        getStoneMachine().setDropItemsToFeet(generalConfig.isDropItemsToFeet());
        getStoneMachine().setDropExpToFeet(generalConfig.isDropExpToFeet());
        getStoneMachine().setAllowHopperOutput(generalConfig.isAllowHopperDropOutput());

        getDropCalculator().setDropMultiplier(new DropMultiplier(generalConfig.getDefaultDropMultiplier(), generalConfig.getMaxDropMultiplier()));
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

            getLogger().log(Level.INFO, "Attempting to load drop entry: "+ entryName);

            final ConfigSectionDropEntry customDropEntry = new ConfigSectionDropEntry(customDropsSection.getConfigurationSection(entryName));

            if(customDropEntry == null) {
                getLogger().log(Level.SEVERE, "Custom Drop Entry equals null value! Skipping...");
                continue;
            }

            dropCalculator.addDrop(customDropEntry.compileDropEntry());

            getLogger().log(Level.INFO, "Loaded custom drop: "+ entryName);

            customDropsCount ++;
        }

        //Reading 'database' configuration
        if(!getConfig().isConfigurationSection("database")) {
            getLogger().log(Level.SEVERE, "Invalid Configuration file (missing \"database\" section)!");
            getLogger().log(Level.SEVERE, "Skipping, database won't work.");
        } else {
            final ConfigSectionDatabase databaseConfig = new ConfigSectionDatabase(getConfig().getConfigurationSection("database"));
            databaseConfig.readDatabaseConnectionDetails();
            sqlManager = new SQLManager(databaseConfig);

            playerSetup.loadPersonalStoneStatsFromDatabase();
            playerSetup.loadPersonalDropConfigFromDatabase();

            getDropCalculator().getDropMultiplier().readPreviousMultiplierFromDatabase();
        }

        getLogger().log(Level.FINE, "Config reloaded!");
        getLogger().log(Level.INFO, "Loaded "+ customDropsCount +" custom drop entries.");
    }

    private void initAsyncAutosave(final long period) {

        getLogger().log(Level.INFO, "Initialized Async Autosave.");

        autosaveRunnable = new BukkitRunnable() {

            @Override
            public void run() {
                int a = 0;

                for(Player player : Bukkit.getServer().getOnlinePlayers()) {
                    final UUID playerUUID = player.getUniqueId();
                    final PersonalDropConfig dropConfig = getPlayerSetup().getPersonalDropConfig(playerUUID);
                    final StoneMachinePlayerStats dropStats = getPlayerSetup().getPlayerStoneMachineStats(playerUUID);

                    getPlayerSetup().savePersonalDropConfigInDatabase(dropConfig);
                    getPlayerSetup().savePersonalStoneStatsInDatabase(dropStats);

                    a++;
                }

                System.out.println("Saved " + a + " players data into the database. Next Save in " + period + " minutes");
            }

        };
    }

    public PlayerSetupManager getPlayerSetup() {
        return playerSetup;
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

    public SQLManager getDatabaseController() {
        return sqlManager;
    }

    @NotNull
    @Override
    public FileConfiguration getConfig() {
        return super.getConfig();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic

        getWindowManager().closeAllWindows(); //TODO: NullPointer Exception <- onDisable?

        playerSetup.onDisable();
        sqlManager.onDisable();
    }
}
