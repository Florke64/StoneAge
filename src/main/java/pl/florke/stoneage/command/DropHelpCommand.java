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
import org.jetbrains.annotations.NotNull;
import pl.florke.stoneage.StoneAge;
import pl.florke.stoneage.util.Message;

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
