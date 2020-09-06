/*
 * Copyright Go2Play.pl (c) 2020.
 * Program made for Go2Play Skyblock server. It's not allowed to re-distribute the code.
 * Author: FlrQue
 */

package win.flrque.g2p.stoneage;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import win.flrque.g2p.stoneage.command.*;
import win.flrque.g2p.stoneage.config.DatabaseConfigReader;
import win.flrque.g2p.stoneage.config.DropEntryConfigReader;
import win.flrque.g2p.stoneage.config.GeneralConfigReader;
import win.flrque.g2p.stoneage.config.ToolsConfigReader;
import win.flrque.g2p.stoneage.database.SQLManager;
import win.flrque.g2p.stoneage.database.playerdata.PlayerConfig;
import win.flrque.g2p.stoneage.database.playerdata.PlayerStats;
import win.flrque.g2p.stoneage.database.playerdata.PlayersData;
import win.flrque.g2p.stoneage.drop.DropCalculator;
import win.flrque.g2p.stoneage.drop.DropMultiplier;
import win.flrque.g2p.stoneage.drop.ExperienceCalculator;
import win.flrque.g2p.stoneage.gui.WindowManager;
import win.flrque.g2p.stoneage.listener.*;
import win.flrque.g2p.stoneage.machine.ApplicableTools;
import win.flrque.g2p.stoneage.machine.StoneMachine;
import win.flrque.g2p.stoneage.util.Message;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

public final class StoneAge extends JavaPlugin {

    private StoneMachine stoneMachine;
    private ApplicableTools applicableTools;
    private CommandExecutionController commandExecutionController;
    private PlayersData playerSetup;

    private WindowManager windowManager;
    private DropCalculator dropCalculator;
    private ExperienceCalculator expCalculator;

    private SQLManager sqlManager;
    private BukkitRunnable autosaveRunnable;

    @Override
    public void onEnable() {
        // Plugin startup logic
        windowManager = new WindowManager();
        dropCalculator = new DropCalculator();
        expCalculator = new ExperienceCalculator();
        playerSetup = new PlayersData();

        initStoneMachines();
        stoneMachine.registerCraftingRecipe();

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

        getServer().getPluginManager().registerEvents(new PlayerSaveDataOnLeaveListener(), this);
        getServer().getPluginManager().registerEvents(new WindowClickListener(), this);

        getServer().getPluginManager().registerEvents(new StatisticsIncreaseListener(), this);
        getServer().getPluginManager().registerEvents(new MinerLevelUpListener(), this);

        getServer().getPluginManager().registerEvents(new DropMultiplierCallListener(), this);

        //Registering Plugin Commands
        getCommand("drop").setExecutor(new DropCommand());
        getCommand("drophelp").setExecutor(new DropHelpCommand());
        getCommand("dropstat").setExecutor(new DropStatCommand());
        getCommand("multiplier").setExecutor(new DropMultiplierCommand());

        final long minute = 60 * 20;
        final long period = 15;

        initAsyncAutosave(period);
        if(autosaveRunnable != null)
            autosaveRunnable.runTaskTimerAsynchronously(this, period * minute, period * minute);

        getDropCalculator().getDropMultiplier().initMultiplierBossBar();
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

        final GeneralConfigReader generalConfig = new GeneralConfigReader(getConfig().getConfigurationSection("machines"));
        generalConfig.compile();

        getStoneMachine().setStoneRespawnFrequency(generalConfig.getStoneFrequency());
        getStoneMachine().setRepairCooldown(generalConfig.getRepairCoolDown());
        getStoneMachine().setDropItemsToFeet(generalConfig.isDropItemsToFeet());
        getStoneMachine().setDropExpToFeet(generalConfig.isDropExpToFeet());
        getStoneMachine().setAllowHopperOutput(generalConfig.isAllowHopperDropOutput());
        getStoneMachine().setAllowHopperInput(generalConfig.isAllowCoalUpgradesByHopper());
        this.commandExecutionController = new CommandExecutionController(generalConfig.getCommandsCoolDown());
        this.expCalculator.setMaximumMinerLevel(generalConfig.getMaxMinerLevel());

        getDropCalculator().setDropMultiplier(new DropMultiplier(generalConfig.getDefaultDropMultiplier(), generalConfig.getMaxDropMultiplier()));

        //Reading applicable tools (pickaxes and their levels)
        final ToolsConfigReader toolsConfig = new ToolsConfigReader(getConfig().getConfigurationSection("tools"));
        toolsConfig.compile();

        applicableTools = new ApplicableTools(toolsConfig.getMachineDestroyTool());
        for(final Material tool: toolsConfig.getMiningTools()) {
            this.applicableTools.addApplicableTool(tool, toolsConfig.getToolLevel(tool));
        }

        //Reading Primitive Stone drop
        if(!getConfig().isConfigurationSection("primitive_drop")) {
            getLogger().log(Level.SEVERE, "Invalid Configuration file (missing \"primitive_drop\" section)!");
            getLogger().log(Level.SEVERE, this.getName() + " plugin will now be disabled.");
            getPluginLoader().disablePlugin(this);
            return;
        }

        final DropEntryConfigReader primitiveDropEntry = new DropEntryConfigReader(getConfig().getConfigurationSection("primitive_drop"));
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

            final DropEntryConfigReader customDropEntry = new DropEntryConfigReader(customDropsSection.getConfigurationSection(entryName));

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
            final DatabaseConfigReader databaseConfig = new DatabaseConfigReader(getConfig().getConfigurationSection("database"));
            databaseConfig.readDatabaseConnectionDetails();
            sqlManager = new SQLManager(databaseConfig);

            new BukkitRunnable() {
                @Override
                public void run() {
                    final long dbLoadStartTime = System.currentTimeMillis();

                    //Loading data of all players from database
                    final int statsCount = playerSetup.loadPersonalStoneStatsFromDatabase();
                    final int configsCount = playerSetup.loadPersonalDropConfigFromDatabase();

                    final long dbLoadFinishTime = System.currentTimeMillis();

                    getLogger().log(Level.INFO, "Loaded drop stats and config from database (in number of "+ statsCount +" and "+ configsCount + ")");
                    getLogger().log(Level.INFO, "Loading from database took " + (dbLoadFinishTime - dbLoadStartTime) + "ms");
                }
            }.runTaskAsynchronously(this);

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
                int savedCount = 0;

                for(Player player : Bukkit.getServer().getOnlinePlayers()) {
                    final UUID playerUUID = player.getUniqueId();
                    final PlayerConfig dropConfig = getPlayerSetup().getPersonalDropConfig(playerUUID);
                    final PlayerStats dropStats = getPlayerSetup().getPlayerStoneMachineStats(playerUUID);

                    try {
                        getPlayerSetup().savePersonalDropConfigInDatabase(dropConfig);
                        getPlayerSetup().savePersonalStoneStatsInDatabase(dropStats);
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }

                    savedCount++;
                }

                final Message success = new Message("Auto-save: Saved $_1 players data into the database | Next Auto-Save in $_2 minutes");
                success.setVariable(1, Integer.toString(savedCount));
                success.setVariable(2, Long.toString(period));

                StoneAge.this.getLogger().log(Level.INFO, success.getPreparedMessage().get(0));
            }
        };
    }

    public PlayersData getPlayerSetup() {
        return playerSetup;
    }

    public StoneMachine getStoneMachine() {
        return this.stoneMachine;
    }

    public CommandExecutionController getCommandExecutionController() {
        return commandExecutionController;
    }

    public ApplicableTools getApplicableTools() {
        return applicableTools;
    }

    public WindowManager getWindowManager() {
        return this.windowManager;
    }

    public DropCalculator getDropCalculator() {
        return dropCalculator;
    }

    public ExperienceCalculator getExpCalculator() {
        return expCalculator;
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
        this.getLogger().log(Level.INFO, "Called plugin's onDisable() method. Bye cruel world!");

        if(getDropCalculator() != null && getDropCalculator().getDropMultiplier() != null && getDropCalculator().getDropMultiplier().getMultiplierBossBar() != null)
            getDropCalculator().getDropMultiplier().getMultiplierBossBar().removeAll();

        this.getLogger().log(Level.INFO, "Closing all Window Manager's GUIs... ");
        getWindowManager().closeAllWindows();

        this.getLogger().log(Level.INFO, "Syncing all unsaved data with the databasse...");
        playerSetup.onDisable();

        this.getLogger().log(Level.INFO, "Disconnecting database, closing connection pool...");
        sqlManager.onDisable();
    }

}
