package win.flrque.g2p.stoneage.listener;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Dispenser;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import win.flrque.g2p.stoneage.StoneAge;

public class StoneMachineBreakListener implements Listener {

    private final StoneAge plugin;

    public StoneMachineBreakListener() {
        plugin = StoneAge.getPlugin(StoneAge.class);
    }

    @EventHandler
    public void onStoneMachineBreak(BlockBreakEvent event) {
        if(event.isCancelled())
            return;

        if(event.getPlayer() == null)
            return;

        final Player destroyer = event.getPlayer();
        final GameMode gameMode = destroyer.getGameMode();

        if(gameMode.equals(GameMode.CREATIVE) || gameMode.equals(GameMode.SPECTATOR))
            return;

        if(!plugin.getStoneMachine().isStoneMachine(event.getBlock()))
            return;

        event.setDropItems(false);

        final ItemStack tool = destroyer.getInventory().getItemInMainHand();
        if(tool.getType().equals(Material.GOLD_PICKAXE)) {
            final Block brokenBlock = event.getBlock();
            final Location brokenBlockLocation = brokenBlock.getLocation();

            for(Player user : plugin.getWindowManager().getWindow((Dispenser) brokenBlock.getState()).getUsers()) {
                if(user != null && user.isOnline()) {
                    user.closeInventory();
                    user.sendMessage("Ta stoniarka zostala zniszczona.");
                }
            }

            brokenBlockLocation.getWorld().dropItemNaturally(brokenBlockLocation, plugin.getStoneMachine().createStoneMachineItem());
        } else {
            event.setCancelled(true);
            destroyer.sendMessage("Stoniarka moze byc usunieta tylko przy pomocy zlotego kilofa!");
        }
    }

}
