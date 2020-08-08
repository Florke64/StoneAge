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
import org.bukkit.scheduler.BukkitRunnable;
import win.flrque.g2p.stoneage.StoneAge;
import win.flrque.g2p.stoneage.gui.window.DropInfoWindow;

public class DropCommand implements CommandExecutor {

    private final StoneAge plugin;

    public DropCommand() {
        plugin = StoneAge.getPlugin(StoneAge.class);
    }

    /**
     * Executes the given command, returning its success
     *
     * @param sender  Source of the command
     * @param command Command which was executed
     * @param label   Alias of the command which was used
     * @param args    Passed command arguments
     * @return true if a valid command, otherwise false
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player)) {
            sender.sendMessage("Tylko Gracz moze wykonac te komende!");
            return true;
        }

        if(args.length == 1 && (args[0].equalsIgnoreCase("help") || args[0].equalsIgnoreCase("pomoc"))) {
            ((Player) sender).performCommand("drophelp");
            return true;
        }

        if(args.length == 1 && (args[0].equalsIgnoreCase("stat") || args[0].equalsIgnoreCase("stats"))) {
            ((Player) sender).performCommand("dropstat");
            return true;
        }

        final Player player = (Player) sender;
        final DropInfoWindow window = new DropInfoWindow(player);

        new BukkitRunnable() {
            @Override
            public void run() {

                window.updateInventoryContent();

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        window.open(player);
                    }
                }.runTask(plugin);
            }
        }.runTaskAsynchronously(plugin);

        return true;
    }

}
