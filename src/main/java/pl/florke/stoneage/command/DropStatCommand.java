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
import pl.florke.stoneage.StoneAge;
import pl.florke.stoneage.database.playerdata.PlayerStats;
import pl.florke.stoneage.database.playerdata.PlayersData;
import pl.florke.stoneage.drop.DropCalculator;
import pl.florke.stoneage.drop.DropEntry;
import pl.florke.stoneage.drop.ExperienceCalculator;
import pl.florke.stoneage.util.Message;

public class DropStatCommand implements CommandExecutor {

    private final StoneAge plugin;
    private final CommandExecutionController executionController;

    private final PlayersData playerSetupManager;
    private final DropCalculator dropCalculator;
    private final ExperienceCalculator experienceCalculator;

    public DropStatCommand() {
        this.plugin = StoneAge.getPlugin(StoneAge.class);
        this.executionController = plugin.getCommandExecutionController();

        this.playerSetupManager = plugin.getPlayersData();
        this.dropCalculator = plugin.getDropCalculator();
        this.experienceCalculator = plugin.getExpCalculator();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            new Message("&cTa komende moze wykonac tylko gracz.").send(sender);
            return false;
        }

        if (!executionController.onCommandExecute(sender)) {
            new Message("&cOdczekaj chwile przed wykonaniem kolejnej komendy.").send(sender);
            return true;
        }

        executionController.recordCommandExecution(sender);

        Player targetPlayer = null;
        if (args.length == 1) {
            targetPlayer = Bukkit.getServer().getPlayerExact(args[0]);
        } else {
            targetPlayer = ((Player) sender);
        }

        if (targetPlayer == null) {
            new Message("&cBlad! Nie znaleziono podanego gracza (Online).").send(sender);

            return false;
        }

        final PlayerStats playerStatistics = playerSetupManager.getPlayerStoneMachineStats(targetPlayer.getUniqueId());
        printPlayerStatistics(sender, playerStatistics);

        return true;
    }

    private void printPlayerStatistics(@NotNull final CommandSender sender, @NotNull final PlayerStats playerStats) {

        final long miningExp = playerStats.getMinerExp();
        final int miningLevel = playerStats.getMinerLvl();

        final Message message = new Message();
        message.addLines(Message.EMPTY);
        message.addLines("&6== &7Statystyki (&c$_5&7) &6==");
        message.addLines("&7Poziom gornictwa: &6$_1");
        message.addLines("&7Poziom doswiadczenie: &6$_2 &7/ &6$_3");
        message.addLines(Message.EMPTY);

        int summary = 0;
        for (DropEntry dropEntry : dropCalculator.getDropEntries()) {
            if (dropEntry == dropCalculator.getPrimitiveDropEntry())
                continue;

            summary += playerStats.getStatistic(dropEntry.getEntryName());
        }

        message.addLines("&7W sumie wykopanych &6$_4&7 roznych przedmiotow.");

        final long nextLevelExperience = experienceCalculator.getExpNeededToLevel(miningLevel + 1);
        message.setVariable(1, Integer.toString(miningLevel));
        message.setVariable(2, Long.toString(miningExp));
        message.setVariable(3, Long.toString(nextLevelExperience));
        message.setVariable(4, Integer.toString(summary));
        message.setVariable(5, playerStats.getPlayerName());

        message.send(sender);
    }
}
