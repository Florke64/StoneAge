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

import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandExecutor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import pl.florke.stoneage.command.*;
import pl.florke.stoneage.config.DatabaseConfigReader;
import pl.florke.stoneage.config.GeneralConfigReader;
import pl.florke.stoneage.config.MachinesConfigReader;
import pl.florke.stoneage.database.DatabaseManager;
import pl.florke.stoneage.database.playerdata.PlayersData;
import pl.florke.stoneage.database.wrapper.DatabaseWrapper;
import pl.florke.stoneage.drop.DropCalculator;
import pl.florke.stoneage.drop.DropEntry;
import pl.florke.stoneage.drop.DropMultiplier;
import pl.florke.stoneage.drop.ExperienceCalculator;
import pl.florke.stoneage.gui.WindowManager;
import pl.florke.stoneage.listener.*;
import pl.florke.stoneage.machine.ResourceSpawner;
import pl.florke.stoneage.machine.StoneMachine;
import pl.florke.stoneage.util.Language;
import pl.florke.stoneage.util.Message;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public final class StoneAge extends JavaPlugin {

    private final PluginListenerRegistry pluginListenerRegistry = new PluginListenerRegistry(this);
    private final List<Class<? extends Listener>> listeners = new ArrayList<>(List.of(
            // Stone Machines
            StoneMachinePlaceListener.class, StoneMachineBreakListener.class,
            StoneMachineInteractListener.class, StoneMachineHopperInteractListener.class,
            StoneMachineRedstoneInteractListener.class, StoneBreakListener.class,
            // Windows, Stats, Player Data
            WindowClickListener.class, DropMultiplierCallListener.class,
            StatisticsIncreaseListener.class, MinerLevelUpListener.class,
            PlayerSaveDataOnLeaveListener.class
    ));

    private final PluginCommandsController commandExecutionController = new PluginCommandsController(this);
    private final Map<String, Class<? extends CommandExecutor>> commandExecutors = new HashMap<>(Map.of(
        "drop", DropCommand.class,
        "drophelp", DropHelpCommand.class,
        "dropstat", DropStatCommand.class,
        "multiplier", DropMultiplierCommand.class,
        "dropspamdb", DropSpamDBCommand.class
    ));

    private StoneMachine stoneMachine;
    private PlayersData playersData;

    private WindowManager windowManager;
    private DropCalculator dropCalculator;
    private ExperienceCalculator expCalculator;

    private Language language;
    private DatabaseManager dbManager;
    private final MachinesConfigReader machinesConfigManager = new MachinesConfigReader(this);

    @Override
    public void onEnable() {
        // Dependency check
        Message.initMessenger(this);
        language = new Language("lang");
        language.reload();

        // Plugin startup logic
        stoneMachine = new StoneMachine();
        windowManager = new WindowManager();
        dropCalculator = new DropCalculator();
        expCalculator = new ExperienceCalculator();
        playersData = new PlayersData();

        //Saving and reloading config
        saveDefaultConfig();
        reloadConfig();

        //Registering Event Listeners for the Plugin
        for (final Class<? extends Listener> listener : listeners)
            pluginListenerRegistry.registerListener(listener);

        //Registering Plugin Commands
        for (final Map.Entry<String, Class<? extends CommandExecutor>> cmdExec : commandExecutors.entrySet())
            commandExecutionController.registerExecutor(cmdExec.getKey(), cmdExec.getValue());

        //TODO: Add auto-save period setting to the config.yml (open issue #17)

        // DBManager is initialized in reloadConfig()
        getDBManager().initAsyncAutosave(15);
        getDropCalculator().getDropMultiplier().initMultiplierBossBar();
    }

    @Override
    public void saveDefaultConfig() {
        //saving config.yml
        super.saveDefaultConfig();

        //saving resources from directories
        // "drops/" and "drops/resources/"
        machinesConfigManager.saveDefaultDrops();
    }

    @Override
    public void reloadConfig() throws AssertionError {
        new Message("Reloading the configuration file...").log(Level.INFO);
        super.reloadConfig();

        //Reading 'General' configuration for Stone Machines
        final ConfigurationSection machinesSection = getConfig().getConfigurationSection("machines");
        if (machinesSection == null || !getConfig().isConfigurationSection("machines")) {
            new Message("Invalid Configuration file (missing the \"machines\" section)!", "$_1 plugin will now be disabled.")
                .placeholder(1, this.getName()).log(Level.SEVERE);

            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        final GeneralConfigReader generalConfig = new GeneralConfigReader(machinesSection);

        stoneMachine.registerCraftingRecipe();
        stoneMachine.applyMachineConfiguration(generalConfig);

        this.commandExecutionController.setCommandCooldownSeconds(generalConfig.getCommandsCoolDown());
        this.expCalculator.setMaximumMinerLevel(generalConfig.getMaxMinerLevel());

        final DropMultiplier defaultMultiplier = new DropMultiplier(generalConfig.getDefaultDropMultiplier(), generalConfig.getMaxDropMultiplier());
        getDropCalculator().setDropMultiplier(defaultMultiplier);

        //Reading 'DropEntry' configuration
        for (final DropEntry dropResourceEntry : machinesConfigManager.getDropResourceEntries())
            dropCalculator.addDropResource(dropResourceEntry);

        for (final DropEntry dropEntry : machinesConfigManager.getCustomDropEntries())
            dropCalculator.addCustomDrop(dropEntry);

        reloadResourceRelations(generalConfig);
        reloadDatabaseConfig(generalConfig);

        new Message("Config reloaded!").log(Level.INFO);
    }

    private void reloadResourceRelations(GeneralConfigReader generalConfig) {
        final ResourceSpawner resourceSpawner = stoneMachine.getResourceSpawner();

        for (final Map.Entry<String, ArrayList<String>> entry : generalConfig.getResourceRelations().entrySet()) {
            final String resourceName = entry.getKey();
            final ArrayList<String> dropNames = entry.getValue();

            // Getting KEY <String, ...>
            final String rawResourceKeyName = DropEntry.EntryType.RESOURCE_DROP.getPrefix() + resourceName;
            final NamespacedKey resourceKey = new NamespacedKey(this, rawResourceKeyName.toLowerCase());
            final DropEntry resource = dropCalculator.getDropEntry(resourceKey);

            // Getting VALUES <..., List<String>>
            for (final String dropName : dropNames) {
                final String rawCustomDropKeyName = DropEntry.EntryType.CUSTOM_DROP.getPrefix() + dropName;
                final NamespacedKey dropKey = new NamespacedKey(this, rawCustomDropKeyName.toLowerCase());
                final DropEntry drop = dropCalculator.getDropEntry(dropKey);

                //apply relation
                resourceSpawner.addResourceChild(resource, drop);
            }
        }
    }

    private void reloadDatabaseConfig(GeneralConfigReader generalConfig) {
        if (!getConfig().isConfigurationSection("database")) {
            new Message("Invalid Configuration file (missing the \"database\" section)!",
                    "Skipping, database won't work.").log(Level.SEVERE);
            getServer().getPluginManager().disablePlugin(this);
        }

        final ConfigurationSection databaseSection = getConfig().getConfigurationSection("database");
        final DatabaseConfigReader databaseConfig = new DatabaseConfigReader(databaseSection);
        databaseConfig.readDatabaseConnectionDetails();

        // Load player statistics (& drop preferences)
        dbManager = new DatabaseManager(databaseConfig);
        dbManager.loadAllPlayers();

        // Read previous Multiplier (before restart)
        dbManager.getSQLWrapper().readPreviousMultiplierFromDatabase(dropCalculator.getDropMultiplier());
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

    public PluginCommandsController getCommandExecutionController() {
        return commandExecutionController;
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

    public DatabaseManager getDBManager() {
        return dbManager;
    }

    public DatabaseWrapper getSQLWrapper() {
        return getDBManager().getSQLWrapper();
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

        if (getDropCalculator() != null
                && getDropCalculator().getDropMultiplier() != null
                && getDropCalculator().getDropMultiplier().getMultiplierBossBar() != null)
            getDropCalculator().getDropMultiplier().getMultiplierBossBar().removeAll();

        new Message("Closing all Window Manager's GUIs... ").log(Level.INFO);
        getWindowManager().closeAllWindows();

        new Message("Syncing all unsaved data with the database...").log(Level.INFO);
        if (playersData != null) playersData.onDisable();
        else new Message("PlayersData not even initialized!").log(Level.WARNING);

        new Message("Disconnecting database, closing connection pool...").log(Level.INFO);
        if (dbManager != null) dbManager.onDisable();
        else new Message("Database not even initialized!").log(Level.WARNING);
    }

}
