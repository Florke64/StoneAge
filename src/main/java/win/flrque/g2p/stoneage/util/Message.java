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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Message {

    public static final String EMPTY = "";

    private String linePrefix = "&l&8> ";

    //TODO: Automatically color different data types
    private ChatColor genericTextColor = ChatColor.GRAY;
    private ChatColor variableValueColor = ChatColor.RED;
    private ChatColor numberValueColor = ChatColor.GOLD;

    private final Map<Integer, String> variables = new HashMap<>();

    private final List<String> rawMessage = new ArrayList<>();
    private final List<String> message = new ArrayList<>();

    private final Logger logger = StoneAge.getPlugin(StoneAge.class).getLogger();

    private boolean usePrefixOnSend = false;

    public Message() {
        this.rawMessage.clear();
    }

    public Message(@NotNull String ...message) {
        for(final String line : message) {
            this.rawMessage.add(line);
        }

        prepare();
    }

    public Message(@NotNull List<String> message) {
        for(final String line : message) {
            this.rawMessage.add(line);
        }

        prepare();
    }

    public void broadcastToTheServer() {
        for(final String line : this.message) {
            Bukkit.getServer().broadcastMessage((usePrefixOnSend? linePrefix : "") + line);
        }
    }

    public void send(@NotNull CommandSender target) {
        for(final String line : this.message) {
            target.sendMessage((usePrefixOnSend? linePrefix : "") + line);
        }
    }

    public void sendActionMessage(@NotNull Player target) {
        for(final String line : this.message) {
            final BaseComponent chatComponent = new TextComponent((usePrefixOnSend? linePrefix : "") + line);
            target.spigot().sendMessage(ChatMessageType.ACTION_BAR, chatComponent);
        }
    }

    public void logToConsole() {
        logToConsole(Level.INFO, null);
    }

    public void logToConsole(@NotNull final Level level, @Nullable final String signature) {
        final String logPrefixSignature = signature == null? "" : signature + ": ";

        for(final String line : this.message) {
            logger.log(level, logPrefixSignature + line);
        }
    }

    public void addLines(@NotNull final String ...lines) {
        for(final String line : lines) {
            this.rawMessage.add(line);
        }

        prepare();
    }

    public void addLines(@NotNull final List<String> lines) {
        for(final String line : lines) {
            this.rawMessage.add(line);
        }

        prepare();
    }

    public void clear() {
        this.rawMessage.clear();
        this.message.clear();
    }

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
        for(int i=0; i<this.rawMessage.size(); i++) {
            final String currentLine = this.rawMessage.get(i);
            final String updatedLine = insertVariableValues(currentLine);

            this.rawMessage.set(i, updatedLine);
        }
    }

    private String insertVariableValues(String line) {
        for(int v : this.variables.keySet()) {
            final String variable = capitalize(this.variables.get(v));
            line = line.replace(("$_"+v), variable);
        }

        return line;
    }

    private void color() {
        for(int i=0; i<this.rawMessage.size(); i++) {
            final String coloredLine = color(this.rawMessage.get(i));
            this.message.add(coloredLine);
        }
    }

    public void setUsePrefixOnSend(boolean usePrefix) {
        this.usePrefixOnSend = usePrefix;
    }

    public boolean isUsingPrefixOnSend() {
        return usePrefixOnSend;
    }

    public static String color(String text) {
        if(text == null || text.isEmpty()) {
            return text;
        }

        return ChatColor.translateAlternateColorCodes('&', text);
    }

    public static String simplePrepare(String textLine) {
        if(textLine == null || textLine.isEmpty()) {
            return textLine;
        }

        String result = color(textLine);
        result = replaceUnderlines(result);
        result = capitalize(result);

        return result;
    }

    public static String capitalize(String text) {
        if(text == null || text.isEmpty()) {
            return text;
        }

        return text.substring(0, 1).toUpperCase() + text.substring(1);
    }

    public static String replaceUnderlines(String text) {
        if(text == null || text.isEmpty()) {
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

}
