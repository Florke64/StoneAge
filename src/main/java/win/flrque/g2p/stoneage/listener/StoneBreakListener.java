/*
 * Copyright Go2Play.pl (c) 2020.
 * Program made for Go2Play Skyblock server. It's not allowed to re-distribute the code.
 * Author: FlrQue
 */

package win.flrque.g2p.stoneage.listener;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Dispenser;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import win.flrque.g2p.stoneage.StoneAge;
import win.flrque.g2p.stoneage.drop.DropLoot;

public class StoneBreakListener implements Listener {

    private final StoneAge plugin;

    public StoneBreakListener() {
        plugin = StoneAge.getPlugin(StoneAge.class);
    }

    @EventHandler
    public void onStoneBreak(BlockBreakEvent event) {
        if(event.isCancelled()) return;

        final Player player = event.getPlayer();
        if(player == null) return;

        final Block brokenBlock = event.getBlock();
        if(!brokenBlock.getType().equals(Material.STONE)) return;

        @SuppressWarnings("deprecation")
        byte stoneType = brokenBlock.getState().getData().getData();
        if(stoneType != ((byte) 0)) return;

        final Block machineBlock = plugin.getStoneMachine().getConnectedStoneMachine(brokenBlock);
        if(machineBlock != null){

            //Cancelling default drops
            event.setDropItems(false);

            //Replacing broken stone with new one

            final GameMode playerGameMode = player.getGameMode();
            if(playerGameMode.equals(GameMode.CREATIVE) || playerGameMode.equals(GameMode.SPECTATOR))
                return;

            final ItemStack usedTool = player.getInventory().getItemInMainHand();
            final DropLoot finalDrop = plugin.getDropCalculator().calculateDrop(player, usedTool, (Dispenser) machineBlock.getState());

            dropLoot(player.getLocation(), brokenBlock.getLocation(), machineBlock.getLocation(), finalDrop);
            player.sendMessage("Udalo ci sie wykopac " + finalDrop.getItemStack().getType() + " x" + finalDrop.getItemStack().getAmount());

            new BukkitRunnable() {
                @Override
                public void run() {
                    plugin.getStoneMachine().generateStone(brokenBlock.getLocation());
                }
            }.runTaskLater(plugin, 1l);
        }
    }

    private void dropLoot(Location playerLoc, Location stoneLoc, Location machineLoc, DropLoot dropLoot) {
        final Location expDropLocation = (plugin.getStoneMachine().isDropExpToFeet()) ? playerLoc : stoneLoc;
        final Location itemDropLocation = (plugin.getStoneMachine().isDropItemsToFeet()) ? playerLoc : stoneLoc;

        if(dropLoot.getExp() > 0) {
            final Entity orb = expDropLocation.getWorld().spawnEntity(expDropLocation, EntityType.EXPERIENCE_ORB);
            ((ExperienceOrb) orb).setExperience(dropLoot.getExp());
        }

        if(dropLoot.getItemStack() != null) {
            itemDropLocation.getWorld().dropItemNaturally(itemDropLocation, dropLoot.getItemStack());
        }
    }

}
