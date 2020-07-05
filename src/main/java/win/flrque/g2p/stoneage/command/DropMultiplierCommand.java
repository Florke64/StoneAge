/*
 * Copyright Go2Play.pl (c) 2020.
 * Program made for Go2Play Skyblock server. It's not allowed to re-distribute the code.
 * Author: FlrQue
 */

package win.flrque.g2p.stoneage.command;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import win.flrque.g2p.stoneage.StoneAge;

public class DropMultiplierCommand implements CommandExecutor {

    private final StoneAge plugin;

    public DropMultiplierCommand() {
        plugin = StoneAge.getPlugin(StoneAge.class);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, String label, String[] args) {
        if(!sender.hasPermission(command.getPermission()))
            return false;

        if(args.length == 0) {

            //TODO: Show current multiplier info
            return true;
        }

        float multiplier;
        try {
            multiplier = Float.parseFloat(args[0]);
        } catch (NumberFormatException ex) {
            sender.sendMessage("Podano bledny mnoznik!");
            ex.printStackTrace();
            return false;
        }

        long time = 100000;
        if(args.length == 2) {
            try {
                time = Long.parseLong(args[1]) * 60 * 1000;
            } catch (NumberFormatException ex) {
                sender.sendMessage("Podano bledny czas dla mnoznika!");
                ex.printStackTrace();
                return false;
            }
        }

        boolean success = plugin.getDropCalculator().getDropMultiplier().setDropMultiplier(multiplier, time);
        if(success) {
            Bukkit.broadcastMessage(sender.getName() + " dal mnoznik x" + multiplier + " na czas " + (time / 60 / 1000) + " minut");
        } else {
            sender.sendMessage("Serwer odmowil ustawienia takiego mnoznika!");
        }

        return true;
    }
}
