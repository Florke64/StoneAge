package win.flrque.g2p.stoneage.listener;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import win.flrque.g2p.stoneage.StoneAge;

public class StoneBreakListener implements Listener {

    private final StoneAge plugin;
    private final BlockFace[] machineDirections = new BlockFace[6];

    public StoneBreakListener() {
        plugin = StoneAge.getPlugin(StoneAge.class);
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

        if(plugin.getStoneMachine().isConnectedToStoneMachine(brokenBlock)){

            //TODO: Proceed with Stone Machine's drops
            //TODO: Setup sheduler to replace broken stone
            event.setDropItems(false);
        }
    }

}
