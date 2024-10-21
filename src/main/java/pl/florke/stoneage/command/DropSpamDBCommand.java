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
import pl.florke.stoneage.database.playerdata.PlayersData;
import pl.florke.stoneage.util.Message;

import java.util.UUID;

public class DropSpamDBCommand implements CommandExecutor {

    private final StoneAge plugin;
    private final PlayersData playerSetup;

    public DropSpamDBCommand() {
        plugin = StoneAge.getPlugin(StoneAge.class);
        playerSetup = plugin.getPlayersData();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("g2p.stone.admin")) {
            new Message("&cNie posiadasz wystarczajacych uprawnien do wykonania tej komendy.").send(sender);
            return false;
        }

        if (args.length < 1) {
            new Message("&cNie wystarczajaca ilosc argumentow do wykonania komendy.").send(sender);
            return false;
        }

        try {
            final int entries = Integer.parseInt(args[0]);
            createRandomPlayerStats(entries);

            new Message("&2Wykonano!").send(sender);
            return true;

        } catch (NumberFormatException ex) {
            new Message("&cPodano nie wlasciwy argument.").send(sender);
        }

        return true;
    }

    private final void createRandomPlayerStats(final int n) {
        for (int i = 0; i < n; i++) {
            final UUID uuid = UUID.randomUUID();

            playerSetup.getPlayerStoneMachineStats(uuid).markUnsaved(true);
            playerSetup.getPersonalDropConfig(uuid).markUnsaved(true);
        }
    }

}
