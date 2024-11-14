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

package pl.florke.stoneage;

import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import pl.florke.stoneage.util.Message;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class PluginCommandsController {

    private final JavaPlugin plugin;

    private int commandCooldownSeconds;
    private long commandCooldownMillis;

    private final Map<UUID, Long> commandExecutionHistory = new HashMap<>();

    public PluginCommandsController(@NotNull final JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void setCommandCooldownSeconds(int commandCooldownSeconds) {
        this.commandCooldownSeconds = commandCooldownSeconds;
        this.commandCooldownMillis = commandCooldownSeconds * 1000L;
    }

    public int getCommandCooldownSeconds() {
        return commandCooldownSeconds;
    }

    public long getCommandCooldownMillis() {
        return commandCooldownMillis;
    }

    private boolean isCooldown(@NotNull final UUID playerUniqueId) {
        final long currentTime = System.currentTimeMillis();
        final long lastExecution = this.commandExecutionHistory.getOrDefault(playerUniqueId, 0L);

        return (lastExecution + commandCooldownMillis) > currentTime;
    }

    public boolean isCooldown(@NotNull final CommandSender sender) {
        if (!(sender instanceof Player)) return true;
        final UUID playerUniqueId = ((Player) sender).getUniqueId();

        //Check if cooldown passed
        return isCooldown(playerUniqueId);
    }

    public void recordCommandExecution(@NotNull final CommandSender sender) {
        if (!(sender instanceof Player)) return;
        final UUID playerUniqueId = ((Player) sender).getUniqueId();

        final long currentTime = System.currentTimeMillis();
        commandExecutionHistory.put(playerUniqueId, currentTime);
    }

    public void registerExecutor(@NotNull final String commandLabel, @NotNull final Class<? extends CommandExecutor> commandExecutor) {
        final PluginCommand command = plugin.getCommand(commandLabel);
        if (command == null) {
            final Message error = new Message("Couldn't set CommandExecutor for $_1: Command is null!");
            error.placeholder(1, commandLabel);
            error.log(Level.SEVERE);

            return;
        }

        final Constructor<?>[] executorConstructors = commandExecutor.getConstructors();
        if (executorConstructors.length < 1)
            return;

        try {
            final CommandExecutor newCommandExecutor = (CommandExecutor) executorConstructors[0].newInstance();

            command.setExecutor(newCommandExecutor);
        } catch (ReflectiveOperationException ex) {
            new Message("Cannot register Command Executor: " + commandExecutor.getName(), ex.getMessage()).log(Level.SEVERE);
        }
    }
}
