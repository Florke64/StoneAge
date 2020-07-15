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

import java.util.ArrayList;
import java.util.List;

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
            final List<Message> playerMessage = new ArrayList<>();
            playerMessage.add(new Message("&6* * * * * &7Informacje o mnozniku kamienia &6* * * * *"));

            DropMultiplier multiplier = plugin.getDropCalculator().getDropMultiplier();
            if(!multiplier.isActive()) {
                playerMessage.add(new Message("  &cMnoznik dropu nie jest aktywowany."));
            } else {
                playerMessage.add(new Message("  &2Mnoznik jest aktywny, drop &7x&6" + multiplier.getCurrentDropMultiplier() +"&2,"));
                playerMessage.add(new Message("  &2bedzie aktywny jeszcze &6" + multiplier.getMinutesLeft() + " &7minut&2."));
                playerMessage.add(new Message("  &2Aktywowany przez: &8" + multiplier.getCallerName() + "&2."));
            }

            for(Message m : playerMessage) {
                m.send(sender);
            }

            return true;
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
        if(success) {
            final List<Message> playerMessage = new ArrayList<>();
            playerMessage.add(new Message("&6* * * * * &7UWAGA! &6* * * * *"));

            playerMessage.add(new Message("  &2Mnoznik dropu z kamienia aktywowany przez: &8" + multiplier.getCallerName() + "&2."));
            playerMessage.add(new Message("  &2Drop surowcow, teraz &7x&6" + multiplier.getCurrentDropMultiplier() +"&2!"));
            playerMessage.add(new Message("  &2Aktywowany na czas &6" + (multiplierValue/1000/60) + " &7minut&2."));
            playerMessage.add(new Message("  "));

            for(Message m : playerMessage) {
                m.send(sender);
            }
        } else {
            new Message("&4Serwer odmowil ustawienia takiego mnoznika! &c(Event was cancelled?)").send(sender);
        }

        return true;
    }
}
