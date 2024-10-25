/*
 * @Florke64 <Daniel Chojnacki>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package pl.florke.stoneage;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import pl.florke.stoneage.command.*;
import pl.florke.stoneage.config.DatabaseConfigReader;
import pl.florke.stoneage.config.DropEntryConfigReader;
import pl.florke.stoneage.config.GeneralConfigReader;
import pl.florke.stoneage.config.ToolsConfigReader;
import pl.florke.stoneage.database.SQLManager;
import pl.florke.stoneage.database.playerdata.PlayersData;
import pl.florke.stoneage.drop.DropCalculator;
import pl.florke.stoneage.drop.DropMultiplier;
import pl.florke.stoneage.drop.ExperienceCalculator;
import pl.florke.stoneage.gui.WindowManager;
import pl.florke.stoneage.listener.*;
import pl.florke.stoneage.machine.ApplicableTools;
import pl.florke.stoneage.machine.StoneMachine;
import pl.florke.stoneage.util.Language;
import pl.florke.stoneage.util.Message;

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

    private Language language;
    private SQLManager sqlManager;


    @Override
    public void onEnable() {
        // Dependency check
        Message.initMessenger(this);
        language = new Language("lang");

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
        registerPluginCommandExecutor("drop", new DropCommand());
        registerPluginCommandExecutor("drophelp", new DropHelpCommand());
        registerPluginCommandExecutor("dropstat", new DropStatCommand());
        registerPluginCommandExecutor("multiplier", new DropMultiplierCommand());
        registerPluginCommandExecutor("dropspamdb", new DropSpamDBCommand());

        //TODO: Add this setting to the config.yml (open issue #17)
        final long period = 15; // autosave period in minutes

        getDatabaseController().initAsyncAutosave(period);
        getDropCalculator().getDropMultiplier().initMultiplierBossBar();
    }

    private void registerPluginCommandExecutor(@NotNull final String commandLabel, @NotNull final CommandExecutor commandExecutor) {
        final PluginCommand command = getCommand(commandLabel);
        if (command == null) {
            final Message error = new Message("&4Couldn't set CommandExecutor for /$_1: Command is null!");
            error.placeholder(1, commandLabel);
            error.log(Level.SEVERE);

            return;
        }

        command.setExecutor(commandExecutor);
    }

    private void initStoneMachines() {
        final List<String> machineLore = StoneMachine.createDefaultMachineLore();

        // TODO: Move to config.yml
        new Message(language.getText("stone-machine-item-name")).log(Level.INFO);
        final String machineName = new Message(language.getText("stone-machine-item-name"))
                .getCachedCompiledMessage().getFirst();

        stoneMachine = new StoneMachine(machineName, machineLore);
    }

    @Override
    public void reloadConfig() throws AssertionError {
        new Message("Reloading the configuration file...").log(Level.INFO);
        super.reloadConfig();

        //Reading 'General' configuration for Stone Machines
        if (!getConfig().isConfigurationSection("machines")) {
            new Message("Invalid Configuration file (missing the \"machines\" section)!")
                .addLines("$_1 plugin will now be disabled.")
                .placeholder(1, this.getName())
                    .log(Level.SEVERE);

            Bukkit.getServer().getPluginManager().disablePlugin(this);
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
            error.placeholder(1, this.getName());
            error.log(Level.SEVERE);

            Bukkit.getServer().getPluginManager().disablePlugin(this);

            return;
        }

        final DropEntryConfigReader primitiveDropEntry = new DropEntryConfigReader(getConfig().getConfigurationSection("primitive_drop"));
        dropCalculator.setPrimitiveDrop(primitiveDropEntry.compileDropEntry());

        //Reading Custom Stone drop
        if (!getConfig().isConfigurationSection("custom_drops")) {
            final Message error = new Message();
            error.addLines("Invalid Configuration file (missing the \"custom_drops\" section)!");
            error.addLines("Skipping, stone will drop server-default items.");
            error.log(Level.SEVERE);

            return;
        }

        int customDropsLoaded = 0, customDropsFound = 0;
        final ConfigurationSection customDropsSection = getConfig().getConfigurationSection("custom_drops");

        if (customDropsSection == null) {
            final Message error = new Message();
            error.addLines("Invalid Configuration file (missing the \"custom_drops\" section)!");
            error.addLines("Skipping, Stone will drop server-default values.");
            error.log(Level.SEVERE);
        } else {
            for (String entryName : customDropsSection.getKeys(false)) {
                customDropsFound++;

                final Message loadingMessage = new Message("Attempting to load drop entry: $_1");
                loadingMessage.placeholder(1, entryName);
                loadingMessage.log(Level.INFO);

                final DropEntryConfigReader customDropEntry = new DropEntryConfigReader(customDropsSection.getConfigurationSection(entryName));

                dropCalculator.addDrop(customDropEntry.compileDropEntry());

                final Message success = new Message("Loaded a custom drop: $_1");
                success.placeholder(1, entryName);
                success.log(Level.INFO);

                customDropsLoaded++;
            }
        }

        final Message customDropsInfo = new Message("Loaded $_1 of $_2 custom drop entries.");
        customDropsInfo.placeholder(1, Integer.toString(customDropsLoaded));
        customDropsInfo.placeholder(2, Integer.toString(customDropsFound));
        customDropsInfo.log(Level.INFO);

        //Reading 'database' configuration
        if (!getConfig().isConfigurationSection("database")) {
            final Message error = new Message();
            error.addLines("Invalid Configuration file (missing the \"database\" section)!");
            error.addLines("Skipping, database won't work.");
            error.log(Level.SEVERE);
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
                    success.placeholder(1, Integer.toString(statsCount));
                    success.placeholder(2, Integer.toString(configsCount));
                    success.placeholder(3, Long.toString(dbLoadFinishTime - dbLoadStartTime));
                    success.log(Level.INFO);

                }
            }.runTaskAsynchronously(this);

            getDropCalculator().getDropMultiplier().readPreviousMultiplierFromDatabase();
        }

        new Message("Config reloaded!").log(Level.INFO);
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

    public Language getLanguage() {
        return language;
    }

    public String getLanguage(final String languageKey) {
        return getLanguage(languageKey, 0);
    }

    public String getLanguage(final String languageKey, int line) {
        return language.getText(languageKey)[line];
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        new Message("Called plugin's onDisable() method. Bye cruel world!").log(Level.INFO);

        if (getDropCalculator() != null && getDropCalculator().getDropMultiplier() != null && getDropCalculator().getDropMultiplier().getMultiplierBossBar() != null)
            getDropCalculator().getDropMultiplier().getMultiplierBossBar().removeAll();

        new Message("Closing all Window Manager's GUIs... ").log(Level.INFO);
        getWindowManager().closeAllWindows();

        new Message("Syncing all unsaved data with the database...").log(Level.INFO);
        playersData.onDisable();

        new Message("Disconnecting database, closing connection pool...").log(Level.INFO);
        sqlManager.onDisable();
    }

}
