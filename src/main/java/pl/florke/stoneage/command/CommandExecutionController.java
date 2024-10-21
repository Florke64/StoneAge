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

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import pl.florke.stoneage.StoneAge;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CommandExecutionController {

    private final StoneAge plugin;

    private final int commandCooldownSeconds;
    private final long commandCooldownMillis;

    private final Map<UUID, Long> commandExecutionHistory = new HashMap<>();

    public CommandExecutionController(final int commandCooldownSeconds) {
        this.plugin = StoneAge.getPlugin(StoneAge.class);

        this.commandCooldownSeconds = commandCooldownSeconds;
        this.commandCooldownMillis = commandCooldownSeconds * 1000;
    }

    public int getCommandCooldownSeconds() {
        return commandCooldownSeconds;
    }

    public long getCommandCooldownMillis() {
        return commandCooldownMillis;
    }

    private boolean isAllowedToExecuteYet(@NotNull final UUID playerUniqueId) {
        final long currentTime = System.currentTimeMillis();
        final long lastExecution = this.commandExecutionHistory.getOrDefault(playerUniqueId, 0l);

        return (lastExecution + commandCooldownMillis) <= currentTime;
    }

    public boolean onCommandExecute(@NotNull final CommandSender sender) {
        if (!(sender instanceof Player)) return true;
        final UUID playerUniqueId = ((Player) sender).getUniqueId();

        //Check if cooldown passed
        return isAllowedToExecuteYet(playerUniqueId);
    }

    public void recordCommandExecution(@NotNull final CommandSender sender) {
        if (!(sender instanceof Player)) return;
        final UUID playerUniqueId = ((Player) sender).getUniqueId();

        final long currentTime = System.currentTimeMillis();
        commandExecutionHistory.put(playerUniqueId, currentTime);
    }
}
