/*
 * Copyright Go2Play.pl (c) 2020.
 * Program made for Go2Play Skyblock server. It's not allowed to re-distribute the code.
 * Author: FlrQue
 */

package win.flrque.g2p.stoneage.util;

import org.bukkit.ChatColor;

public class Message {

    public static String prettify(String text) {
        if(text == null || text.isEmpty()) {
            return text;
        }

        String result = ChatColor.translateAlternateColorCodes('&', text);
        result = replaceUnderlines(result);
        result = capitalize(result);

        return result;
    }

    public static String capitalize(String text) {
        if(text == null || text.isEmpty()) {
            return text;
        }

        return text.substring(0, 1).toUpperCase() + text.substring(1).toLowerCase();
    }

    public static String replaceUnderlines(String text) {
        if(text == null || text.isEmpty()) {
            return text;
        }

        return text.replace('_', ' ');
    }

}
