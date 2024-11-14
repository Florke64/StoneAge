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
import pl.florke.stoneage.PluginCommandsController;
import pl.florke.stoneage.StoneAge;
import pl.florke.stoneage.util.Language;
import pl.florke.stoneage.util.Message;

public class DropHelpCommand implements CommandExecutor {

    private final PluginCommandsController executionController;
    private final Language lang;

    public DropHelpCommand() {
        final StoneAge plugin = StoneAge.getPlugin(StoneAge.class);

        this.executionController = plugin.getCommandExecutionController();
        this.lang = plugin.getLanguage();


    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (executionController.isCooldown(sender)) {
            new Message(lang.getText("command-error-cooldown")).send(sender);
            return true;
        }

        executionController.recordCommandExecution(sender);

        new Message(lang.getText("command-feedback-drop-help")).send(sender);

        return true;
    }
}
