package win.flrque.g2p.stoneage.listener;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Dispenser;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.material.Directional;
import win.flrque.g2p.stoneage.StoneAge;

import static org.bukkit.block.BlockFace.*;

public class StoneBreakListener implements Listener {

    private final StoneAge plugin;
    private final BlockFace[] machineDirections = new BlockFace[6];

    public StoneBreakListener() {
        plugin = StoneAge.getPlugin(StoneAge.class);

        machineDirections[0] = NORTH;
        machineDirections[1] = BlockFace.SOUTH;
        machineDirections[2] = UP;
        machineDirections[3] = DOWN;
        machineDirections[4] = BlockFace.EAST;
        machineDirections[5] = BlockFace.WEST;
    }

    @EventHandler
    public void onStoneBreak(BlockBreakEvent event) {
        if(event.isCancelled()) return;

        final Player player = event.getPlayer();
        if(player == null) return;

        final GameMode playerGameMode = player.getGameMode();
        if(playerGameMode.equals(GameMode.CREATIVE) || playerGameMode.equals(GameMode.SPECTATOR))
            return;

        final Block brokenBlock = event.getBlock();
        if(!brokenBlock.getType().equals(Material.STONE)) return;

        byte stoneType = brokenBlock.getState().getData().getData();
        if(stoneType != ((byte) 0)) return;

        for(int i=0; i<machineDirections.length; i++) {
            final Block relativeBlock = brokenBlock.getRelative(machineDirections[i], 1);

            if(plugin.getStoneMachine().isStoneMachine(relativeBlock)) {
                final Dispenser stoneMachine = (Dispenser) relativeBlock.getState();
                final Directional direction = (Directional) stoneMachine.getData();

                if(counterFace(direction.getFacing()).equals(machineDirections[i])) {
                    player.sendMessage("to stoniarka!");
                    event.setDropItems(false);

                    //TODO: Proceed with Stone Machine's drops
                }
            }
        }

    }

    private BlockFace counterFace(BlockFace face) {
        //TODO: Maybe tweak it a bit?
        switch (face) {
            case UP: return DOWN;
            case DOWN: return UP;
            case NORTH: return SOUTH;
            case SOUTH: return NORTH;
            case EAST: return WEST;
            case WEST: return EAST;
            default: return null;
        }
    }

}
