/*
 * Copyright Go2Play.pl (c) 2020.
 * Program made for Go2Play Skyblock server. It's not allowed to re-distribute the code.
 * Author: FlrQue
 */

package win.flrque.g2p.stoneage.util;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class Message {

    public static final String EMPTY = "";

    private String message = "";
    private boolean usePrefixOnSend = true;

    public Message(String message) {
        this.message = message;
    }

    public void send(@NotNull CommandSender target) {
        target.sendMessage(prettify(this.message));
    }

    public String getRawMessage() {
        return message;
    }

    public void setVariable(int n, String value) {
        this.message = message.replace(("$_"+n), value);
    }

    public void setUsePrefixOnSend(boolean usePrefix) {
        this.usePrefixOnSend = usePrefix;
    }

    public boolean isUsingPrefixOnSend() {
        return usePrefixOnSend;
    }

    public static String prettify(String text) {
        if(text == null || text.isEmpty()) {
            return text;
        }

        String result = color(text);
        result = replaceUnderlines(result);
        result = capitalize(result);

        return result;
    }

    @NotNull
    public static String color(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
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

}
