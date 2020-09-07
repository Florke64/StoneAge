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
import win.flrque.g2p.stoneage.database.playerdata.PlayersData;
import win.flrque.g2p.stoneage.util.Message;

import java.util.UUID;

public class DropSpamDBCommand implements CommandExecutor {

    private final StoneAge plugin;
    private final PlayersData playerSetup;

    public DropSpamDBCommand() {
        plugin = StoneAge.getPlugin(StoneAge.class);
        playerSetup = plugin.getPlayersData();
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
        if (!sender.hasPermission("g2p.stone.admin")) {
            new Message("&cNie posiadasz wystarczajacych uprawnien do wykonania tej komendy.").send(sender);
            return false;
        }

        if (args.length < 1) {
            new Message("&cNie wystarczajaca ilosc argumentow do wykonania komendy.").send(sender);
            return false;
        }

        try {
            final int entries = Integer.parseInt(args[0]);
            createRandomPlayerStats(entries);

            new Message("&2Wykonano!").send(sender);
            return true;

        } catch (NumberFormatException ex) {
            new Message("&cPodano nie wlasciwy argument.").send(sender);
        }

        return true;
    }

    private final void createRandomPlayerStats(final int n) {
        for (int i = 0; i < n; i++) {
            final UUID uuid = UUID.randomUUID();

            playerSetup.getPlayerStoneMachineStats(uuid).markUnsaved(true);
            playerSetup.getPersonalDropConfig(uuid).markUnsaved(true);
        }
    }

}
