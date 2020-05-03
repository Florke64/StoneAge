/*
 * Copyright Go2Play.pl (c) 2020.
 * Program made for Go2Play Skyblock server. It's not allowed to re-distribute the code.
 * Author: FlrQue
 */

package win.flrque.g2p.stoneage.command;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class DropHelpCommand implements CommandExecutor {

    private final List<String> helpContent = new ArrayList<>();

    public DropHelpCommand() {
        helpContent.add(" ");
        helpContent.add("&6&l== &5Stoniarki &6Go2Play &6&l==");
        helpContent.add("&7Postaw stoniarke w wybranym przez siebie");
        helpContent.add("&7kierunku - w tym kierunku bedzie sie generowal kamien.");
        helpContent.add("&4Uwaga! &eStoniarki mozna niszczyc tylko zlotymi kilofami.");
        helpContent.add(" ");
        helpContent.add("&aStoniarki mozna ulepszac &7za walute serwerowa oraz wegiel.");
        helpContent.add("&7Aby dowiedziec sie wiecej o ulepszeniach, otworz panel");
        helpContent.add("&7stoniarki - klikajac prawym przyciskiem myszy na maszyne.");
        helpContent.add("&7- - - - - - - - - - ");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        for(String line : helpContent)
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', line));

        return true;
    }

}
