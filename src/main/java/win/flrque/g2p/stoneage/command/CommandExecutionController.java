/*
 * Copyright Go2Play.pl (c) 2020.
 * Program made for Go2Play Skyblock server. It's not allowed to re-distribute the code.
 * Author: FlrQue
 */

package win.flrque.g2p.stoneage.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import win.flrque.g2p.stoneage.StoneAge;

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
        if(!(sender instanceof Player)) return true;
        final UUID playerUniqueId = ((Player) sender).getUniqueId();

        //Check if cooldown passed
        return isAllowedToExecuteYet(playerUniqueId);
    }

    public void recordCommandExecution(@NotNull final CommandSender sender) {
        if(!(sender instanceof Player)) return;
        final UUID playerUniqueId = ((Player) sender).getUniqueId();

        final long currentTime = System.currentTimeMillis();
        commandExecutionHistory.put(playerUniqueId, currentTime);
    }
}
