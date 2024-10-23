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
import pl.florke.stoneage.drop.DropMultiplier;
import pl.florke.stoneage.util.Language;
import pl.florke.stoneage.util.Message;

public class DropMultiplierCommand implements CommandExecutor {

    private final Language lang;
    private final DropMultiplier multiplier;

    public DropMultiplierCommand() {
        lang = StoneAge.getPlugin(StoneAge.class).getLanguage();
        multiplier = StoneAge.getPlugin(StoneAge.class).getDropCalculator().getDropMultiplier();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, String label, String[] args) {
        if (args.length == 0) {
            printMultiplierInfo(sender, multiplier, false);

            return true;
        }

        if (!sender.hasPermission("stoneage.admin")) {
            new Message(lang.getText("command-error-cmd-permission-deny")).send(sender);
            return false;
        }

        float multiplierValue;
        try {
            multiplierValue = Float.parseFloat(args[0]);
        } catch (NumberFormatException ex) {
            new Message(lang.getText("command-feedback-invalid-multiplier")).send(sender);
            return false;
        }

        long time = 100000;
        if (args.length == 2) {
            try {
                time = Long.parseLong(args[1]) * 60 * 1000;
            } catch (NumberFormatException ex) {
                new Message(lang.getText("command-feedback-invalid-time")).send(sender);
                ex.printStackTrace();
                return false;
            }
        }

        boolean success = multiplier.setDropMultiplier(sender, multiplierValue, time);

        if (success) printMultiplierInfo(sender, multiplier, true);
        else {
            new Message(lang.getText("command-error-cmd-usage")).send(sender);
        }

        return true;
    }

    private void printMultiplierInfo(@NotNull final CommandSender commandSender, @NotNull final DropMultiplier multiplier, boolean broadcast) {
        final Message multiplierMessage = new Message();

        if (broadcast)
            multiplierMessage.addLines(lang.getText("command-feedback-multiplier-broadcast"));
        else
            multiplierMessage.addLines(lang.getText("command-feedback-multiplier-info"));

        if (!multiplier.isActive()) {
            multiplierMessage.addLines(lang.getText("command-feedback-multiplier-active"));

            multiplierMessage.replacePlaceholder(1, multiplier.getCallerName());
            multiplierMessage.replacePlaceholder(2, Float.toString(multiplier.getCurrentDropMultiplier()));
            multiplierMessage.replacePlaceholder(3, Integer.toString(multiplier.getMinutesLeft()));
        } else {
            multiplierMessage.addLines(lang.getText("command-feedback-multiplier-inactive"));
        }

        if (broadcast) multiplierMessage.broadcast();
        else multiplierMessage.send(commandSender);

    }
}
