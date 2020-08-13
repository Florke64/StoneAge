/*
 * Copyright Go2Play.pl (c) 2020.
 * Program made for Go2Play Skyblock server. It's not allowed to re-distribute the code.
 * Author: FlrQue
 */

package win.flrque.g2p.stoneage.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import win.flrque.g2p.stoneage.StoneAge;
import win.flrque.g2p.stoneage.event.DropMultiplierStartEvent;

import java.sql.SQLException;
import java.util.UUID;
import java.util.logging.Level;

public class DropMultiplierCallListener implements Listener {

    private final StoneAge plugin;

    public DropMultiplierCallListener() {
        this.plugin = StoneAge.getPlugin(StoneAge.class);
    }

    @EventHandler
    public void onDropMultiplierCall(final DropMultiplierStartEvent event) {
        new BukkitRunnable() {
            @Override
            public void run() {
                final String callerName = event.getCallerName();
                final UUID callerUUID = event.getCallerUniqueId();
                final float value = event.getValue();
                final long startMillis = event.getStartTime();
                final long timeoutTime = event.getTimeout();

                try {
                    plugin.getDatabaseController().insertDropMultiplierRecord(callerName, callerUUID, value, startMillis, timeoutTime);
                } catch (SQLException ex) {
                    plugin.getLogger().log(Level.WARNING, "Unable to save multiplier data into the database! Multiplier won't recover after server restart!");
                    ex.printStackTrace();
                }
            }
        }.runTaskAsynchronously(plugin);
    }

}