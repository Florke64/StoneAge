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

import org.bukkit.Location;
import org.bukkit.block.Dispenser;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import pl.florke.stoneage.drop.DropLoot;

public class StoneMachineStoneBreakEvent extends Event {

    public static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final DropLoot loot;
    private final Dispenser stoneMachine;
    private final Location location;

    public StoneMachineStoneBreakEvent(Player player, @NotNull Dispenser stoneMachine, DropLoot loot) {
        this.player = player;
        this.loot = loot;
        this.stoneMachine = stoneMachine;
        this.location = stoneMachine.getLocation();
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public Dispenser getStoneMachine() {
        return stoneMachine;
    }

    public DropLoot getLoot() {
        return loot;
    }

    public Location getLocation() {
        return location;
    }

    public Player getPlayer() {
        return player;
    }

    @Override
    public String getEventName() {
        return super.getEventName();
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

}
