/*
 * Copyright Go2Play.pl (c) 2020.
 * Program made for Go2Play Skyblock server. It's not allowed to re-distribute the code.
 * Author: FlrQue
 */

package win.flrque.g2p.stoneage.util;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import win.flrque.g2p.stoneage.StoneAge;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Message {

    public static final String EMPTY = "";

    private static final List<String> mutedConsoleLogSignatures = new ArrayList<>();

    private String linePrefix = "&l&8> ";

    //TODO: Automatically color different data types
//    private ChatColor genericTextColor = ChatColor.GRAY;
//    private ChatColor variableValueColor = ChatColor.RED;
//    private ChatColor numberValueColor = ChatColor.GOLD;

    private final Map<Integer, String> variables = new HashMap<>();

    private final List<String> rawMessage = new ArrayList<>();
    private final List<String> message = new ArrayList<>();

    private final Logger logger = StoneAge.getPlugin(StoneAge.class).getLogger();

    private boolean usePrefixOnSend = false;

    public Message(@NotNull String... message) {
        this.rawMessage.addAll(Arrays.asList(message));

        prepare();
    }

    public Message(@NotNull List<String> message) {
        this.rawMessage.addAll(message);

        prepare();
    }

    public void broadcastToTheServer() {
        for (final String line : this.message) {
            Bukkit.getServer().broadcastMessage((usePrefixOnSend ? linePrefix : "") + line);
        }
    }

    public void send(@NotNull CommandSender target) {
        for (final String line : this.message) {
            target.sendMessage((usePrefixOnSend ? linePrefix : "") + line);
        }
    }

    public void sendActionMessage(@NotNull Player target) {
        for (final String line : this.message) {
            final BaseComponent chatComponent = new TextComponent((usePrefixOnSend ? linePrefix : "") + line);
            target.spigot().sendMessage(ChatMessageType.ACTION_BAR, chatComponent);
        }
    }

    @SuppressWarnings("unused")
    public void logToConsole() {
        logToConsole(Level.INFO, (String) null);
    }

    @SuppressWarnings("unused")
    public void logToConsole(@Nullable final String signature) {
        logToConsole(Level.INFO, signature);
    }

    @SuppressWarnings("unused")
    public void logToConsole(@NotNull final LogTag logTag) {
        logToConsole(Level.INFO, logTag.toString());
    }

    @SuppressWarnings("unused")
    public void logToConsole(@NotNull final Level level) {
        logToConsole(level, (String) null);
    }

    @SuppressWarnings("unused")
    public void logToConsole(@NotNull final Level level, @NotNull final LogTag logTag) {
        logToConsole(level, logTag.toString());
    }

    public void logToConsole(@NotNull final Level level, @Nullable final String signature) {
        if (mutedConsoleLogSignatures.contains(signature))
            return;

        final String logPrefixSignature = signature == null ? "" : capitalize(signature) + ": ";

        for (final String line : this.message) {
            logger.log(level, logPrefixSignature + line);
        }
    }

    @SuppressWarnings("unused")
    public void addLines(@NotNull final String... lines) {
        this.rawMessage.addAll(Arrays.asList(lines));

        prepare();
    }

    @SuppressWarnings("unused")
    public void addLines(@NotNull final List<String> lines) {
        this.rawMessage.addAll(lines);

        prepare();
    }

    @SuppressWarnings("unused")
    public void clear() {
        this.rawMessage.clear();
        this.message.clear();
    }

    @SuppressWarnings("unused")
    public void setVariable(int n, String value) {
        this.variables.put(n, value);

        prepare();
    }

    private void prepare() {
        //Clearing old prepared message
        this.message.clear();

        insertVariableValues();
        color();
    }

    private void insertVariableValues() {
        for (int i = 0; i < this.rawMessage.size(); i++) {
            final String currentLine = this.rawMessage.get(i);
            final String updatedLine = insertVariableValues(currentLine);

            this.rawMessage.set(i, updatedLine);
        }
    }

    private String insertVariableValues(String line) {
        for (int v : this.variables.keySet()) {
            final String variable = capitalize(this.variables.get(v));
            line = line.replace(("$_" + v), variable);
        }

        return line;
    }

    private void color() {
        for (String s : this.rawMessage) {
            final String coloredLine = color(s);
            this.message.add(coloredLine);
        }
    }

    @SuppressWarnings("unused")
    public void setUsePrefixOnSend(boolean usePrefix) {
        this.usePrefixOnSend = usePrefix;
    }

    @SuppressWarnings("unused")
    public boolean isUsingPrefixOnSend() {
        return usePrefixOnSend;
    }

    public static String color(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        return ChatColor.translateAlternateColorCodes('&', text);
    }

    public static String simplePrepare(String textLine) {
        if (textLine == null || textLine.isEmpty()) {
            return textLine;
        }

        String result = color(textLine);
        result = replaceUnderlines(result);
        result = capitalize(result);

        return result;
    }

    public static String capitalize(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        return text.substring(0, 1).toUpperCase() + text.substring(1);
    }

    public static String replaceUnderlines(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        return text.replace('_', ' ');
    }

    public List<String> getRawMessage() {
        return this.rawMessage;
    }

    public List<String> getPreparedMessage() {
        return this.message;
    }

    @Nullable
    public String getPreparedLine(final int n) {
        return this.message.get(n);
    }

    @SuppressWarnings("unused")
    public static void muteConsoleLogSignature(@NotNull final String signature) {
        mutedConsoleLogSignatures.add(signature);
    }

    @SuppressWarnings("unused")
    public static void unmuteConsoleLogSignature(@NotNull final String signature) {
        mutedConsoleLogSignatures.remove(signature);
    }

}
