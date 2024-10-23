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

package pl.florke.stoneage.util;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import pl.florke.stoneage.StoneAge;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Message {

    private static final String LINE_PREFIX = "&l&8> ";

    private final Logger logger = StoneAge.getPlugin(StoneAge.class).getLogger();
    private final BukkitAudiences adventure = StoneAge.getPlugin(StoneAge.class).getAdventure();

    // TODO: Automatically color different data types
    // private ChatColor genericTextColor = ChatColor.GRAY;
    // private ChatColor variableValueColor = ChatColor.RED;
    // private ChatColor numberValueColor = ChatColor.GOLD;

    private final List<String> rawMessage = new ArrayList<>();
    private final List<String> cachedCompiledMessage = new ArrayList<>();
    private final Map<Integer, String> variables = new HashMap<>();

    private boolean usePrefixOnSend = false;

    public Message(@NotNull String... message) {
        this.rawMessage.addAll(Arrays.asList(message));
        prepare();
    }

    public Message(@NotNull List<String> message) {
        this.rawMessage.addAll(message);
        prepare();
    }

    @SuppressWarnings("unused")
    public Message addLines(@NotNull final String... lines) {
        this.rawMessage.addAll(Arrays.asList(lines));
        return prepare();
    }

    @SuppressWarnings("unused")
    public Message addLines(@NotNull final List<String> lines) {
        this.rawMessage.addAll(lines);
        return prepare();
    }

    private Message prepare() {
        clearCache();

        // Inserting values in respective $_n variables
        getRawMessage().replaceAll(this::insertVariableValues);

        color();

        return this;
    }

    public void broadcast() {
        final Server server = StoneAge.getPlugin(StoneAge.class).getServer();
        final Collection<? extends Player> players = server.getOnlinePlayers();

        for (Player player : players) {
            send(player);
        }
    }

    public void send(@NotNull Audience recipient) {
        for (final String line : this.cachedCompiledMessage) {
            TextComponent textMessage = Component.text((usePrefixOnSend ? LINE_PREFIX : "") + line);
            recipient.sendMessage(textMessage);
        }
    }

    public void sendActionMessage(@NotNull Player recipient) {
        for (final String line : this.cachedCompiledMessage) {
            final String text = (usePrefixOnSend ? LINE_PREFIX : "") + line;
            final Component actionBarMessage = Component.text(text, NamedTextColor.WHITE);

            final Audience audience = adventure.player(recipient);
            audience.sendActionBar(actionBarMessage);
        }
    }

    public void log(final Level level) {
        for (final String line : this.cachedCompiledMessage) {
            logger.log(level, line);
        }
    }

    @SuppressWarnings("unused")
    public Message replacePlaceholder(int n, String value) {
        this.variables.put(n, value);
        return prepare();
    }

    private String insertVariableValues(String line) {
        for (int var : this.variables.keySet()) {
            final String variable = capitalize(this.variables.get(var));
            line = line.replace(("$_" + var), variable);
        }

        return line;
    }

    private void color() {
        for (String s : this.rawMessage) {
            final String coloredLine = color(s);
            this.cachedCompiledMessage.add(coloredLine);
        }
    }

    public static String color(String text) {
        if (text == null || text.isEmpty()) return text;

        // O.G. method
        return text.replace('&', '\u00a7');
    }

    /**
     * Returns a new string with the first character of {@code text} upper-cased.
     * If the string is empty, it is returned as is.
     */
    public static String capitalize(String text) {
        if (text == null || text.isEmpty()) return text;

        return text.substring(0, 1).toUpperCase() + text.substring(1);
    }

    public static String replaceUnderlines(String text) {
        if (text == null || text.isEmpty()) return text;

        return text.replace('_', ' ');
    }

    /**
     * Prettifies a constant name. Like "DIAMOND_PICKAXE" -> "Diamond Pickaxe"
     * @see #color(String)
     * @see #prepare()
     */
    public static String constNamePrettify(String text) {
        if (text == null || text.isEmpty()) return text;

        String result = color(text);
        result = replaceUnderlines(result);
        result = capitalize(result);

        return result;
    }

    public List<String> getRawMessage() {
        return this.rawMessage;
    }

    public List<String> getCachedCompiledMessage() {
        return this.cachedCompiledMessage;
    }

    @SuppressWarnings("unused")
    public boolean isUsingPrefixOnSend() {
        return usePrefixOnSend;
    }

    @SuppressWarnings("unused")
    public void setUsePrefixOnSend(boolean usePrefix) {
        this.usePrefixOnSend = usePrefix;
    }

    private void clearCache() {
        this.cachedCompiledMessage.clear();
    }

}
