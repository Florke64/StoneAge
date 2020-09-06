/*
 * Copyright Go2Play.pl (c) 2020.
 * Program made for Go2Play Skyblock server. It's not allowed to re-distribute the code.
 * Author: FlrQue
 */

package win.flrque.g2p.stoneage.drop;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import win.flrque.g2p.stoneage.StoneAge;
import win.flrque.g2p.stoneage.database.SQLManager;
import win.flrque.g2p.stoneage.event.DropMultiplierStartEvent;
import win.flrque.g2p.stoneage.util.Message;

import java.sql.*;
import java.util.UUID;
import java.util.logging.Level;

public class DropMultiplier {

    private final StoneAge plugin;

    private final float defaultDropMultiplier;
    private final float maxDropMultiplier;

    private float currentDropMultiplier;
    private long multiplierTimeout = 0;
    private long multiplierSetOn = 0;

    private BukkitRunnable multiplierBossBarRunnable;
    private BossBar multiplierBossBar;

    private String callerName;
    private UUID callerUniqueId;

    public DropMultiplier(float defaultDropMultiplier, float maxDropMultiplier) {
        plugin = StoneAge.getPlugin(StoneAge.class);

        this.defaultDropMultiplier = defaultDropMultiplier;
        this.currentDropMultiplier = defaultDropMultiplier;
        this.maxDropMultiplier = maxDropMultiplier;
    }

    public float getDefaultDropMultiplier() {
        return defaultDropMultiplier;
    }

    public float getMaxDropMultiplier() {
        return maxDropMultiplier;
    }

    public float getCurrentDropMultiplier() {
        return currentDropMultiplier;
    }

    private void setCurrentDropMultiplier(float currentDropMultiplier) {
        this.currentDropMultiplier = currentDropMultiplier;
    }

    public long getMultiplierTimeout() {
        return multiplierTimeout;
    }

    private void setMultiplierTimeout(long multiplierTimeout) {
        this.multiplierTimeout = multiplierTimeout;
    }

    public long getMultiplierStartTime() {
        return multiplierSetOn;
    }

    private void setMultiplierStartTime(long multiplierSetOn) {
        this.multiplierSetOn = multiplierSetOn;
    }

    public UUID getCallerUniqueId() {
        return callerUniqueId;
    }

    public void setCallerUniqueId(UUID callerUniqueId) {
        this.callerUniqueId = callerUniqueId;
    }

    public String getCallerName() {
        return callerName;
    }

    public void setCallerName(String callerName) {
        this.callerName = callerName;
    }

    public boolean setDropMultiplier(@NotNull CommandSender caller, float value, long time) {
        final String callerName = caller.getName();
        final UUID callerUniqueId;

        if (caller instanceof Player) {
            callerUniqueId = ((Player) caller).getUniqueId();
        } else {
            callerUniqueId = UUID.randomUUID();
        }

        return setDropMultiplier(callerName, callerUniqueId, value, time);
    }

    public boolean setDropMultiplier(String callerName, UUID callerUniqueId, float value, long time) {
        if (value <= defaultDropMultiplier || value > maxDropMultiplier)
            return false;

        if (time < (1 * 60 * 1000) || time > (24 * 60 * 60 * 1000))
            return false;

        final long startTime = System.currentTimeMillis();
        final long timeout = System.currentTimeMillis() + time;

        DropMultiplierStartEvent event = new DropMultiplierStartEvent(callerName, callerUniqueId, value, startTime, timeout);
        plugin.getServer().getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            return false;
        }

        setCurrentDropMultiplier(value);
        setMultiplierStartTime(startTime);
        setMultiplierTimeout(timeout);

        setCallerName(callerName);
        setCallerUniqueId(callerUniqueId);

        return true;
    }

    public int getMinutesLeft() {
        return (int) ((getMultiplierTimeout() / 1000 - System.currentTimeMillis() / 1000) / 60);
    }

    public boolean isActive() {
        if (getMultiplierTimeout() < System.currentTimeMillis())
            return false;

        return defaultDropMultiplier != currentDropMultiplier;
    }

    public void readPreviousMultiplierFromDatabase() {
        final StringBuilder query = new StringBuilder();

        query.append("SELECT * FROM `" + SQLManager.TABLE_DROP_MULTIPLIER + "` ");
        query.append("ORDER BY `" + SQLManager.TABLE_DROP_MULTIPLIER + "`.`Timeout` DESC ");
        query.append("LIMIT 1;");

        try (final Connection conn = plugin.getDatabaseController().getConnection();
             final PreparedStatement ps = conn.prepareStatement(query.toString());
             final ResultSet response = ps.executeQuery()) {

            if (response == null) {
                plugin.getLogger().log(Level.WARNING, "Couldn't recover drop multiplier from database!");
                return;
            }

            while (response.next()) {
                final Timestamp startTime = response.getTimestamp("SetOn");
                final Timestamp timeoutTime = response.getTimestamp("Timeout");
                final float multiplierValue = response.getFloat("MultiplierValue");
                final String callerName = response.getString("CallerName");
                final String callerUUID = response.getString("CallerUUID");

                setCurrentDropMultiplier(multiplierValue);
                setMultiplierStartTime(startTime.getTime());
                setMultiplierTimeout(timeoutTime.getTime());

                setCallerName(callerName);
                setCallerUniqueId(UUID.fromString(callerUUID));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public void initMultiplierBossBar() {

        plugin.getLogger().log(Level.INFO, "Initialized Multiplier visualization via Boss Bar.");

        final DropMultiplier multiplier = plugin.getDropCalculator().getDropMultiplier();

        final NamespacedKey bossBarKey = new NamespacedKey(plugin, "multiplier_bossbar");
        multiplierBossBar = Bukkit.createBossBar(bossBarKey, ChatColor.RED + "Go2Play", BarColor.BLUE, BarStyle.SEGMENTED_10);
        multiplierBossBar.setVisible(false);

        multiplierBossBarRunnable = new BukkitRunnable() {

            private boolean activeCheck = false;
            private boolean textSwitch = false;

            @Override
            public void run() {
                multiplierBossBar.removeAll();
                if (!multiplier.isActive()) {
                    if (activeCheck == true) {
                        activeCheck = false;
                        new Message("&cMnoznik dropu zakonczyl sie...").broadcastToTheServer();
                    }

                    multiplierBossBar.setVisible(false);
                    return;
                }

                activeCheck = true;

                updateBossBar();

                multiplierBossBar.setVisible(true);
                this.textSwitch = !this.textSwitch;
            }

            private void updateBossBar() {
                final long fullTime = ((multiplier.getMultiplierTimeout() - multiplier.getMultiplierStartTime()) / 1000) / 60;
                final int leftTime = multiplier.getMinutesLeft();
                final float value = multiplier.getCurrentDropMultiplier();

                final double percentage = ((double) leftTime / (double) fullTime);

                final Message bossBarTitle = new Message();
                bossBarTitle.addLines("&6Mnoznik dropu: &7x&c$_1 &6(aktywny przez &c$_2&7min&6)");
                bossBarTitle.addLines("&eMnoznik dropu z kamienia aktywny, nie przegap okazji!");
                bossBarTitle.addLines("&cMnoznik dropu za chwile sie skonczy!");
                bossBarTitle.setVariable(1, Float.toString(value));
                bossBarTitle.setVariable(2, Integer.toString(leftTime));
                multiplierBossBar.setTitle(bossBarTitle.getPreparedMessage().get(leftTime == 0 ? 2 : textSwitch ? 0 : 1));

                multiplierBossBar.setProgress(percentage);
                multiplierBossBar.setColor(percentage < 0.2d ? BarColor.RED : BarColor.BLUE);

                for (final Player player : Bukkit.getOnlinePlayers()) {
                    multiplierBossBar.addPlayer(player);
                }
            }
        };

        if (multiplierBossBarRunnable != null)
            multiplierBossBarRunnable.runTaskTimerAsynchronously(plugin, 10 * 20, 15 * 20);
    }

    public BossBar getMultiplierBossBar() {
        return multiplierBossBar;
    }

}
