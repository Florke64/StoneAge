package win.flrque.g2p.stoneage.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
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

        final Player player = (Player) sender;
        final DropInfoWindow window = new DropInfoWindow(player);
        window.updateInventoryContent();
        window.open(player);

        return true;
    }

}
