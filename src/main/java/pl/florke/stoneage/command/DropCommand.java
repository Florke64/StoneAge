/*
 * @Florke64 <Daniel Chojnacki>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package pl.florke.stoneage.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import pl.florke.stoneage.PluginCommandsController;
import pl.florke.stoneage.StoneAge;
import pl.florke.stoneage.gui.window.DropInfoWindow;
import pl.florke.stoneage.util.Message;

public class DropCommand implements CommandExecutor {

    private final StoneAge plugin;
    private final PluginCommandsController executionController;

    public DropCommand() {
        plugin = StoneAge.getPlugin(StoneAge.class);
        executionController = plugin.getCommandExecutionController();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!(sender instanceof Player player)) {
            new Message(plugin.getLanguage("command-error-cmd-executor")).send(sender);
            return true;
        }

        if (executionController.isCooldown(sender)) {
            new Message(plugin.getLanguage("command-error-cooldown")).send(sender);
            return true;
        }

        if (args.length == 1 && (args[0].equalsIgnoreCase("help") || args[0].equalsIgnoreCase("pomoc"))) {
            ((Player) sender).performCommand("drophelp");
            return true;
        }

        if (args.length > 0 && (args[0].equalsIgnoreCase("stat") || args[0].equalsIgnoreCase("stats"))) {
            if (args.length > 1) {
                ((Player) sender).performCommand("dropstat " + args[1]);
                return true;
            }

            ((Player) sender).performCommand("dropstat");
            return true;
        }

        executionController.recordCommandExecution(sender);

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
