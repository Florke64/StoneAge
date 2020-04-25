package win.flrque.g2p.stoneage.gui;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class WindowManager {

    private final Map<Player, Window> cachedWindows = new HashMap<>();

    public Window getWindow(Player player) {
        return cachedWindows.get(player);
    }

    public void cacheWindow(Player player, Window window) {
        cachedWindows.put(player, window);
    }

}
