/*
 * Copyright Go2Play.pl (c) 2020.
 * Program made for Go2Play Skyblock server. It's not allowed to re-distribute the code.
 * Author: FlrQue
 */

package win.flrque.g2p.stoneage.event;

import org.bukkit.Location;
import org.bukkit.block.Dispenser;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import win.flrque.g2p.stoneage.drop.DropLoot;

public class StoneMachineStoneBreakEvent extends Event {

    private final Player player;
    private final DropLoot loot;
    private final Dispenser stoneMachine;
    private final Location location;

    public StoneMachineStoneBreakEvent(Player player, Dispenser stoneMachine, DropLoot loot) {
        this.player = player;
        this.loot = loot;
        this.stoneMachine = stoneMachine;
        this.location = stoneMachine.getLocation();
    }

    public static final HandlerList handlers = new HandlerList();

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}
