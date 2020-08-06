/*
 * Copyright Go2Play.pl (c) 2020.
 * Program made for Go2Play Skyblock server. It's not allowed to re-distribute the code.
 * Author: FlrQue
 */

package win.flrque.g2p.stoneage.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import win.flrque.g2p.stoneage.util.Message;

public class DropHelpCommand implements CommandExecutor {

    private final Message pluginHelpMessage = new Message();

    public DropHelpCommand() {
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

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        this.pluginHelpMessage.send(sender);

        return true;
    }

}
