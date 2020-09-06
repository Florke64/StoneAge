/*
 * Copyright Go2Play.pl (c) 2020.
 * Program made for Go2Play Skyblock server. It's not allowed to re-distribute the code.
 * Author: FlrQue
 */

package win.flrque.g2p.stoneage.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import win.flrque.g2p.stoneage.StoneAge;
import win.flrque.g2p.stoneage.util.Message;

public class DropHelpCommand implements CommandExecutor {

    private final StoneAge plugin;
    private final CommandExecutionController executionController;

    private final Message pluginHelpMessage = new Message();

    public DropHelpCommand() {
        this.plugin = StoneAge.getPlugin(StoneAge.class);

        this.executionController = plugin.getCommandExecutionController();

        pluginHelpMessage.addLines(Message.EMPTY);
        pluginHelpMessage.addLines("&6&l== &5Stoniarki &6Go2Play &6&l==");
        pluginHelpMessage.addLines("&7Postaw stoniarke w wybranym przez siebie");
        pluginHelpMessage.addLines("&7kierunku - w tym kierunku bedzie sie generowal kamien.");
        pluginHelpMessage.addLines("&4Uwaga! &eStoniarki mozna niszczyc tylko zlotymi kilofami.");
        pluginHelpMessage.addLines(" ");
        pluginHelpMessage.addLines("&aStoniarki mozna ulepszac &7za walute serwerowa oraz wegiel.");
        pluginHelpMessage.addLines("&7Aby dowiedziec sie wiecej o ulepszeniach, otworz panel");
        pluginHelpMessage.addLines("&7stoniarki - klikajac prawym przyciskiem myszy na maszyne.");
        pluginHelpMessage.addLines("&7- - - - - - - - - - ");
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
        if (!executionController.onCommandExecute(sender)) {
            new Message("&cOdczekaj chwile przed wykonaniem kolejnej komendy.").send(sender);
            return true;
        }

        executionController.recordCommandExecution(sender);

        this.pluginHelpMessage.send(sender);

        return true;
    }
}
