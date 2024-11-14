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

package pl.florke.stoneage.event;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.bukkit.Location;
import org.bukkit.block.TileState;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import pl.florke.stoneage.drop.DropLoot;

public class StoneMachineStoneBreakEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private final Player player;
    private final DropLoot loot;
    private final TileState stoneMachine;
    private final Location location;

    public StoneMachineStoneBreakEvent(Player player, @NotNull TileState stoneMachine, DropLoot loot) {
        this.player = player;
        this.loot = loot;
        this.stoneMachine = stoneMachine;
        this.location = stoneMachine.getLocation();
    }

    public TileState getStoneMachine() {
        return stoneMachine;
    }

    public DropLoot getLoot() {
        return loot;
    }

    @SuppressWarnings("unused")
    public Location getLocation() {
        return location;
    }

    public Player getPlayer() {
        return player;
    }


    // SpotBugs highlights that handlers might be null despite @NotNull annotation
    // But Bukkit requires @NotNull annotation for overwritten getHandlers() method
    @SuppressFBWarnings("NP_NONNULL_RETURN_VIOLATION")
    @Override @NotNull
    public HandlerList getHandlers() {
        return handlers;
    }

    @SuppressWarnings("unused")
    public static HandlerList getHandlerList() {
        return handlers;
    }

}
