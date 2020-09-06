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
import win.flrque.g2p.stoneage.drop.DropMultiplier;
import win.flrque.g2p.stoneage.util.Message;

public class DropMultiplierCommand implements CommandExecutor {

    private final StoneAge plugin;

    public DropMultiplierCommand() {
        plugin = StoneAge.getPlugin(StoneAge.class);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, String label, String[] args) {
        if(args.length == 0) {
            DropMultiplier multiplier = plugin.getDropCalculator().getDropMultiplier();
            printMultiplierInfo(sender, multiplier, false);

            return true;
        }

        if(!sender.hasPermission("g2p.stone.admin")) {
            new Message("&cNie posiadasz wystarczajacych uprawnien do wykonania tej komendy.").send(sender);
            return false;
        }

        float multiplierValue;
        try {
            multiplierValue = Float.parseFloat(args[0]);
        } catch (NumberFormatException ex) {
            new Message("&cPodano niewlasciwa wartosc dla mnoznika!").send(sender);
            return false;
        }

        long time = 100000;
        if(args.length == 2) {
            try {
                time = Long.parseLong(args[1]) * 60 * 1000;
            } catch (NumberFormatException ex) {
                new Message("&cPodano niewlasciwy czas mnoznika!").send(sender);
                ex.printStackTrace();
                return false;
            }
        }

        DropMultiplier multiplier = plugin.getDropCalculator().getDropMultiplier();
        boolean success = multiplier.setDropMultiplier(sender, multiplierValue, time);

        if(success) printMultiplierInfo(sender, multiplier, true);
        else { new Message("&4Serwer odmowil ustawienia takiego mnoznika! &c(Event was cancelled?)").send(sender); }

        return true;
    }

    private void printMultiplierInfo(@NotNull final CommandSender commandSender, @NotNull final DropMultiplier multiplier, boolean broadcast) {
        final Message multiplierMessage = new Message();

        if(broadcast) multiplierMessage.addLines("&6* * * * * &7UWAGA! &6* * * * *");
        else multiplierMessage.addLines("&6* * * * * &7Informacje o mnozniku kamienia &6* * * * *");

        if(!multiplier.isActive()) {
            multiplierMessage.addLines("  &cMnoznik dropu nie jest aktywowany.");
        } else {
            multiplierMessage.addLines("  &2Mnoznik dropu z kamienia aktywowany przez: &8$_1&2.");
            multiplierMessage.addLines("  &2Drop surowcow, teraz &7x&6$_2&2!");
            multiplierMessage.addLines("  &2Aktywny przez &6$_3 &7minut&2.");
            multiplierMessage.addLines(Message.EMPTY);

            multiplierMessage.setVariable(1, multiplier.getCallerName());
            multiplierMessage.setVariable(2, Float.toString(multiplier.getCurrentDropMultiplier()));
            multiplierMessage.setVariable(3, Integer.toString(multiplier.getMinutesLeft()));
        }

        if(broadcast) multiplierMessage.broadcastToTheServer();
        else multiplierMessage.send(commandSender);

    }
}
