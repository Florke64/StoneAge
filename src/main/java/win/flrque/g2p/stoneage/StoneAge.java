/*
 * Copyright Go2Play.pl (c) 2020.
 * Program made for Go2Play Skyblock server. It's not allowed to re-distribute the code.
 * Author: FlrQue
 */

package win.flrque.g2p.stoneage;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import win.flrque.g2p.stoneage.command.*;
import win.flrque.g2p.stoneage.config.DatabaseConfigReader;
import win.flrque.g2p.stoneage.config.DropEntryConfigReader;
import win.flrque.g2p.stoneage.config.GeneralConfigReader;
import win.flrque.g2p.stoneage.config.ToolsConfigReader;
import win.flrque.g2p.stoneage.database.SQLManager;
import win.flrque.g2p.stoneage.database.playerdata.PlayersData;
import win.flrque.g2p.stoneage.drop.DropCalculator;
import win.flrque.g2p.stoneage.drop.DropMultiplier;
import win.flrque.g2p.stoneage.drop.ExperienceCalculator;
import win.flrque.g2p.stoneage.gui.WindowManager;
import win.flrque.g2p.stoneage.listener.*;
import win.flrque.g2p.stoneage.machine.ApplicableTools;
import win.flrque.g2p.stoneage.machine.StoneMachine;
import win.flrque.g2p.stoneage.util.LogTag;
import win.flrque.g2p.stoneage.util.Message;

import java.util.List;
import java.util.logging.Level;

public final class StoneAge extends JavaPlugin {

    private StoneMachine stoneMachine;
    private ApplicableTools applicableTools;
    private CommandExecutionController commandExecutionController;
    private PlayersData playersData;

    private WindowManager windowManager;
    private DropCalculator dropCalculator;
    private ExperienceCalculator expCalculator;

    private SQLManager sqlManager;

    @Override
    public void onEnable() {
        // Plugin startup logic
        windowManager = new WindowManager();
        dropCalculator = new DropCalculator();
        expCalculator = new ExperienceCalculator();
        playersData = new PlayersData();

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
        getCommand("dropspamdb").setExecutor(new DropSpamDBCommand());

        //TODO: Add this setting to the config.yml (open issue #17)
        final long period = 15; // autosave period in minutes

        getDatabaseController().initAsyncAutosave(period);
        getDropCalculator().getDropMultiplier().initMultiplierBossBar();
    }

    private void initStoneMachines() {
        final List<String> machineLore = StoneMachine.createDefaultMachineLore();

        //TODO: replace with the config (lang.yml) values (open issue #18)
        stoneMachine = new StoneMachine("&6&lStoniarka", machineLore);
    }

    @Override
    public void reloadConfig() {
        new Message("Reloading the configuration file...").logToConsole(Level.INFO, LogTag.CONFIG);
        super.reloadConfig();

        //Reading 'General' configuration for Stone Machines
        if (!getConfig().isConfigurationSection("machines")) {
            final Message error = new Message();
            error.addLines("Invalid Configuration file (missing the \"machines\" section)!");
            error.addLines("$_1 plugin will now be disabled.");
            error.setVariable(1, this.getName());
            error.logToConsole(Level.SEVERE, LogTag.CONFIG);

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
        for (final Material tool : toolsConfig.getMiningTools()) {
            this.applicableTools.addApplicableTool(tool, toolsConfig.getToolLevel(tool));
        }

        //Reading Primitive Stone drop
        if (!getConfig().isConfigurationSection("primitive_drop")) {
            final Message error = new Message();
            error.addLines("Invalid Configuration file (missing the \"primitive_drop\" section)!");
            error.addLines("$_1 plugin will now be disabled.");
            error.setVariable(1, this.getName());
            error.logToConsole(Level.SEVERE, LogTag.CONFIG);

            getPluginLoader().disablePlugin(this);
            return;
        }

        final DropEntryConfigReader primitiveDropEntry = new DropEntryConfigReader(getConfig().getConfigurationSection("primitive_drop"));
        dropCalculator.setPrimitiveDrop(primitiveDropEntry.compileDropEntry());

        //Reading Custom Stone drop
        if (!getConfig().isConfigurationSection("custom_drops")) {
            final Message error = new Message();
            error.addLines("Invalid Configuration file (missing the \"custom_drops\" section)!");
            error.addLines("Skipping, stone will drop server-default items.");
            error.logToConsole(Level.SEVERE, LogTag.CONFIG);

            return;
        }

        int customDropsLoaded = 0, customDropsFound = 0;
        final ConfigurationSection customDropsSection = getConfig().getConfigurationSection("custom_drops");
        for (String entryName : customDropsSection.getKeys(false)) {
            customDropsFound++;

            final Message loadingMessage = new Message("Attempting to load drop entry: $_1");
            loadingMessage.setVariable(1, entryName);
            loadingMessage.logToConsole(Level.INFO, LogTag.DEBUG);

            final DropEntryConfigReader customDropEntry = new DropEntryConfigReader(customDropsSection.getConfigurationSection(entryName));

            if (customDropEntry == null) {
                final Message success = new Message("\"Custom Drop Entry equals null value! Skipping the \"$_1\"...");
                success.setVariable(1, entryName);
                success.logToConsole(Level.SEVERE, LogTag.CONFIG);

                continue;
            }

            dropCalculator.addDrop(customDropEntry.compileDropEntry());

            final Message success = new Message("Loaded a custom drop: $_1");
            success.setVariable(1, entryName);
            success.logToConsole(Level.INFO, LogTag.CONFIG);

            customDropsLoaded++;
        }

        final Message customDropsInfo = new Message("Loaded $_1 of $_2 custom drop entries.");
        customDropsInfo.setVariable(1, Integer.toString(customDropsLoaded));
        customDropsInfo.setVariable(2, Integer.toString(customDropsFound));
        customDropsInfo.logToConsole(LogTag.CONFIG);

        //Reading 'database' configuration
        if (!getConfig().isConfigurationSection("database")) {
            final Message error = new Message();
            error.addLines("Invalid Configuration file (missing the \"database\" section)!");
            error.addLines("Skipping, database won't work.");
            error.logToConsole(Level.SEVERE, LogTag.CONFIG);
        } else {
            final DatabaseConfigReader databaseConfig = new DatabaseConfigReader(getConfig().getConfigurationSection("database"));
            databaseConfig.readDatabaseConnectionDetails();
            sqlManager = new SQLManager(databaseConfig);

            new BukkitRunnable() {
                @Override
                public void run() {
                    final long dbLoadStartTime = System.currentTimeMillis();

                    //Loading data of all players from database
                    final int statsCount = playersData.loadPersonalStoneStatsFromDatabase();
                    final int configsCount = playersData.loadPersonalDropConfigFromDatabase();

                    final long dbLoadFinishTime = System.currentTimeMillis();

                    final Message success = new Message();
                    success.addLines("Loaded PlayerStats ($_1) and PlayerConfigs ($_2) from the database");
                    success.addLines("Loading took $_3ms");
                    success.setVariable(1, Integer.toString(statsCount));
                    success.setVariable(2, Integer.toString(configsCount));
                    success.setVariable(3, Long.toString(dbLoadFinishTime - dbLoadStartTime));
                    success.logToConsole(Level.INFO, LogTag.DATABASE);

                }
            }.runTaskAsynchronously(this);

            getDropCalculator().getDropMultiplier().readPreviousMultiplierFromDatabase();
        }

        new Message("Config reloaded!").logToConsole(Level.INFO, LogTag.CONFIG);
    }

    public PlayersData getPlayersData() {
        return playersData;
    }

    /**
     * @see StoneMachine
     */
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

        if (getDropCalculator() != null && getDropCalculator().getDropMultiplier() != null && getDropCalculator().getDropMultiplier().getMultiplierBossBar() != null)
            getDropCalculator().getDropMultiplier().getMultiplierBossBar().removeAll();

        this.getLogger().log(Level.INFO, "Closing all Window Manager's GUIs... ");
        getWindowManager().closeAllWindows();

        this.getLogger().log(Level.INFO, "Syncing all unsaved data with the databasse...");
        playersData.onDisable();

        this.getLogger().log(Level.INFO, "Disconnecting database, closing connection pool...");
        sqlManager.onDisable();
    }

}
