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

package pl.florke.stoneage.drop;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import pl.florke.stoneage.StoneAge;
import pl.florke.stoneage.database.DatabaseManager;
import pl.florke.stoneage.database.wrapper.MySQLWrapper;
import pl.florke.stoneage.event.DropMultiplierStartEvent;
import pl.florke.stoneage.util.Message;

import javax.xml.crypto.Data;
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
        final long startTime = System.currentTimeMillis();

        return setDropMultiplier(callerName, callerUniqueId, value, new Timestamp(time), new Timestamp(startTime));
    }

    public boolean setDropMultiplier(String callerName, UUID callerUniqueId, float value, Timestamp timeoutTime, Timestamp startTime) {
        if (value <= defaultDropMultiplier || value > maxDropMultiplier)
            return false;

        if (timeoutTime.getTime() < (60 * 1000) || timeoutTime.getTime() > (24 * 60 * 60 * 1000))
            return false;

        final long timeout = System.currentTimeMillis() + timeoutTime.getTime();

        final DropMultiplierStartEvent event =
                new DropMultiplierStartEvent(callerName, callerUniqueId, value, startTime.getTime(), timeout);
        plugin.getServer().getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            return false;
        }

        setCurrentDropMultiplier(value);
        setMultiplierStartTime(startTime.getTime());
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

    public void initMultiplierBossBar() {

        new Message("Initialized Multiplier visualization via Boss Bar.").log(Level.INFO);

        final NamespacedKey bossBarKey = new NamespacedKey(plugin, "multiplier_bossbar");
        final String bossBarTitle = plugin.getLanguage("stone-multiplier-title-ticker");
        multiplierBossBar = Bukkit.createBossBar(bossBarKey, Message.color(bossBarTitle), BarColor.BLUE, BarStyle.SEGMENTED_10);
        multiplierBossBar.setVisible(false);

        final BukkitRunnable multiplierBossBarRunnable = new BukkitRunnable() {

            private boolean activeCheck = false;
            private boolean textSwitch = false;

            @Override
            public void run() {
                multiplierBossBar.removeAll();
                if (!DropMultiplier.this.isActive()) {
                    if (activeCheck) {
                        activeCheck = false;
                        new Message(plugin.getLanguage("stone-multiplier-end")).broadcast();
                    }

                    multiplierBossBar.setVisible(false);
                    return;
                }

                activeCheck = true;

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        updateBossBar(textSwitch);
                        multiplierBossBar.setVisible(true);

                    }
                }.runTaskAsynchronously(plugin);

                this.textSwitch = !this.textSwitch;
            }
        };

        multiplierBossBarRunnable.runTaskTimerAsynchronously(plugin, 10 * 20, 15 * 20);
    }

    private void updateBossBar(final boolean textSwitch) {
        final long fullTime = ((getMultiplierTimeout() - getMultiplierStartTime()) / 1000) / 60;
        final int leftTime = getMinutesLeft();
        final float value = getCurrentDropMultiplier();

        final double percentage = ((double) leftTime / (double) fullTime);

        int bossBarTitleId = leftTime == 0 ? 2 : textSwitch ? 0 : 1;

        multiplierBossBar.setTitle(
            new Message(plugin.getLanguage("stone-multiplier-title", bossBarTitleId))
                    .placeholder(1, Float.toString(value))
                    .placeholder(2, Integer.toString(leftTime))
                    .getCachedCompiledMessage().getFirst()
        );

        multiplierBossBar.setProgress(percentage);
        multiplierBossBar.setColor(percentage < 0.2d ? BarColor.RED : BarColor.BLUE);

        for (final Player player : Bukkit.getOnlinePlayers()) {
            multiplierBossBar.addPlayer(player);
        }
    }

    public BossBar getMultiplierBossBar() {
        return multiplierBossBar;
    }

}
