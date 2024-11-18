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
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import pl.florke.stoneage.StoneAge;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressWarnings("CommentedOutCode")
public class Message {
    private static Logger logger;
    private static BukkitAudiences adventure;

    public static void initMessenger(@NotNull JavaPlugin plugin) {
        logger = plugin.getLogger();
        adventure = BukkitAudiences.create(plugin);
    }

    // TODO: Automatically color different data types
    // private ChatColor genericTextColor = ChatColor.GRAY;
    // private ChatColor variableValueColor = ChatColor.RED;
    // private ChatColor numberValueColor = ChatColor.GOLD;

    private final List<String> rawMessage = new ArrayList<>();
    private final List<String> cachedCompiledMessage = new ArrayList<>();
    private final Map<Integer, String> variables = new HashMap<>();

    public Message(final String... message) {
        this(new ArrayList<>(List.of(message)));
    }

    public Message(List<String> message) {
        addLines(message);
    }

    @SuppressWarnings("unused")
    public Message addLines(final String... lines) {
        if (lines == null) return this;

        return addLines(Arrays.asList(lines));
    }

    @SuppressWarnings("unused")
    public Message addLines(final List<String> lines) {
        if (lines == null)
            return this;

        try {
            this.rawMessage.addAll(lines);
        } catch (RuntimeException ex) {
            return this;
        }

        return recalculate();
    }

    private Message recalculate() {
        clearCache();

        // Inserting values in respective $_n variables
        getRawMessage().replaceAll(this::insertVariableValues);

        colors();

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
        for (final String line : getCachedCompiledMessage()) {
            TextComponent textMessage = Component.text(line);
            recipient.sendMessage(textMessage);
        }
    }

    public void sendActionMessage(@NotNull Player recipient) {
        for (final String line : getCachedCompiledMessage()) {
            final Component actionBarMessage = Component.text(line, NamedTextColor.WHITE);

            final Audience audience = adventure.player(recipient);
            audience.sendActionBar(actionBarMessage);
        }
    }

    public void log(final Level level) {
        for (final String line : getCachedCompiledMessage()) {
            logger.log(level, line);
        }
    }

    public Message placeholder(final String ...values) {
        if (values == null) return this;

        for (int i = 0; i < values.length; i++) {
            if (values[i] != null)
                placeholder(i, values[i], false);
        }

        return recalculate();
    }

    public Message placeholder(int n, String value) {
        return placeholder(n, value, true);
    }

    private Message placeholder(int n, String value, boolean recalculate) {
        this.variables.put(n, value);
        return recalculate? recalculate() : this;
    }

    private String insertVariableValues(String line) {
        if (line == null) return "";

        for (Map.Entry<Integer, String> placeholder : this.variables.entrySet()) {
            final int placeholderId = placeholder.getKey();
            if (!line.contains(("$_" + placeholderId)))
                continue;

            String placeholderValue = placeholder.getValue();
            if (placeholderValue == null) {
                placeholderValue = "<?>";
            }

            String value = capitalize(placeholderValue);

            line = line.replace(("$_" + placeholderId), value);
        }

        return line;
    }

    private void colors() {
        for (String s : this.rawMessage) {
            final String coloredLine = colors(s);
            cachedCompiledMessage.add(coloredLine);
        }
    }

    @Deprecated
    public static String colors(String text) {
        if (text == null || text.isEmpty()) return text;

        // O.G. method
        return text.replace('&', '\u00a7');
    }

    @NotNull
    public static TextComponent color(String text) {
        if (text == null || text.isEmpty()) return Component.text("");

        // O.G. method
        final String coloredLine = text.replace('&', '\u00a7');

        return Component.text(coloredLine);
    }

    @NotNull
    public static TextComponent color(TextComponent text) {
        if (text == null) return Component.text("");

        // O.G. method
        final String coloredLine = text.content().replace('&', '\u00a7');

        return Component.text(coloredLine);
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
     * @see #colors(String)
     * @see #recalculate()
     */
    public static String constNamePrettify(String text) {
        if (text == null || text.isEmpty()) return text;

        String result = colors(text);
        result = replaceUnderlines(result);
        result = capitalize(result);

        return result;
    }

    public List<String> getRawMessage() {
        return this.rawMessage;
    }

    private List<String> getCachedCompiledMessage() {
        return cachedCompiledMessage;
    }

    public List<TextComponent> asComponents() {
        List<TextComponent> components = new ArrayList<>();
        for (String s : getCachedCompiledMessage())
            components.add(Component.text(s));

        return components;
    }

    private void clearCache() {
        getCachedCompiledMessage().clear();
    }

}
