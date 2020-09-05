/*
 * Copyright Go2Play.pl (c) 2020.
 * Program made for Go2Play Skyblock server. It's not allowed to re-distribute the code.
 * Author: FlrQue
 */

package win.flrque.g2p.stoneage.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import win.flrque.g2p.stoneage.StoneAge;
import win.flrque.g2p.stoneage.database.playerdata.PlayerStats;
import win.flrque.g2p.stoneage.database.playerdata.PlayersData;
import win.flrque.g2p.stoneage.drop.DropCalculator;
import win.flrque.g2p.stoneage.util.Message;

public class DropStatCommand implements CommandExecutor {

    private final StoneAge plugin;
    private final CommandExecutionController executionController;

    private final PlayersData playerSetupManager;
    private final DropCalculator dropCalculator;

    public DropStatCommand() {
        this.plugin = StoneAge.getPlugin(StoneAge.class);
        this.executionController = plugin.getCommandExecutionController();

        this.playerSetupManager = plugin.getPlayerSetup();
        this.dropCalculator = plugin.getDropCalculator();
    }

    /**
     * Executes the given command, returning its success.
     * <br>
     * If false is returned, then the "usage" plugin.yml entry for this command
     * (if defined) will be sent to the player.
     *
     * @param sender  Source of the command
     * @param command Command which was executed
     * @param label   Alias of the command which was used
     * @param args    Passed command arguments
     * @return true if a valid command, otherwise false
     */
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(!(sender instanceof Player)) {
            new Message("&cTa komende moze wykonac tylko gracz.").send(sender);
            return false;
        }

        if(!executionController.onCommandExecute(sender)) {
            new Message("&cOdczekaj chwile przed wykonaniem kolejnej komendy.").send(sender);
            return true;
        }

        executionController.recordCommandExecution(sender);

        final PlayerStats playerStatistics = playerSetupManager.getPlayerStoneMachineStats(((Player) sender).getUniqueId());
        printPlayerStatistics(sender, playerStatistics);

        return true;
    }

    private void printPlayerStatistics(@NotNull final CommandSender sender, @NotNull final PlayerStats playerStats) {

        final long miningExp = playerStats.getMinerExp();
        final int miningLevel = playerStats.getMinerLvl();

        final Message message = new Message();
        message.addLines(Message.EMPTY);
        message.addLines("&6== &5Statystyki &6==");
        message.addLines("&7Twoj poziom gornictwa: &6$_1");
        message.addLines("&7Twoje doswiadczenie: &6$_2");
        message.addLines(Message.EMPTY);

        for(String statisticKey : playerStats.getStatisticKeys()) {
            final String dropEntryName = dropCalculator.getDropEntry(statisticKey).getCustomName();
            final int dropEntryStatValue = playerStats.getStatistic(statisticKey);
            message.addLines("&7" + Message.capitalize(dropEntryName) + ": &6" + dropEntryStatValue);
        }

        message.setVariable(1, Integer.toString(miningLevel));
        message.setVariable(2, Long.toString(miningExp));

        message.send(sender);
    }
}
