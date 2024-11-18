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

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import pl.florke.stoneage.PluginCommandsController;
import pl.florke.stoneage.StoneAge;
import pl.florke.stoneage.database.playerdata.PlayerStats;
import pl.florke.stoneage.database.playerdata.PlayersData;
import pl.florke.stoneage.drop.DropCalculator;
import pl.florke.stoneage.drop.DropEntry;
import pl.florke.stoneage.drop.ExperienceCalculator;
import pl.florke.stoneage.util.Language;
import pl.florke.stoneage.util.Message;

public class DropStatCommand implements CommandExecutor {

    private final Language lang;
    private final PluginCommandsController executionController;

    private final PlayersData playerSetupManager;
    private final DropCalculator dropCalculator;
    private final ExperienceCalculator experienceCalculator;

    public DropStatCommand() {
        final StoneAge plugin = StoneAge.getPlugin(StoneAge.class);

        this.lang = plugin.getLanguage();
        this.executionController = plugin.getCommandExecutionController();

        this.playerSetupManager = plugin.getPlayersData();
        this.dropCalculator = plugin.getDropCalculator();
        this.experienceCalculator = plugin.getExpCalculator();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            new Message(lang.getText("command-error-cmd-executor")).send(sender);
            return false;
        }

        if (executionController.isCooldown(sender)) {
            new Message(lang.getText("command-error-cooldown")).send(sender);
            return true;
        }

        executionController.recordCommandExecution(sender);

        Player targetPlayer = args.length == 1? Bukkit.getServer().getPlayerExact(args[0]) : ((Player) sender);


        if (targetPlayer == null) {
            new Message(lang.getText("command-error-player-offline")).send(sender);
            return false;
        }

        final PlayerStats playerStatistics = playerSetupManager.getPlayerStoneMachineStats(targetPlayer.getUniqueId());
        printPlayerStatistics(sender, playerStatistics);

        return true;
    }

    private void printPlayerStatistics(@NotNull final CommandSender sender, @NotNull final PlayerStats playerStats) {
        final long miningExp = playerStats.getMinerExp();
        final int miningLevel = playerStats.getMinerLvl();

        final Message message = new Message(lang.getText("command-feedback-drop-print-stats"));

        int summary = 0;
        for (DropEntry dropEntry : dropCalculator.getDropEntries()) {
            if (dropEntry.getEntryType().equals(DropEntry.EntryType.PRIMITIVE))
                continue;

            summary += playerStats.getStatistic(dropEntry.getKey());
        }

        message.addLines(lang.getText("command-feedback-drop-print-summary"));

        final long nextLevelExperience = experienceCalculator.getExpNeededToLevel(miningLevel + 1);
        message.placeholder(1, Integer.toString(miningLevel));
        message.placeholder(2, Long.toString(miningExp));
        message.placeholder(3, Long.toString(nextLevelExperience));
        message.placeholder(4, Integer.toString(summary));
        message.placeholder(5, playerStats.getPlayerName());

        message.send(sender);
    }
}
