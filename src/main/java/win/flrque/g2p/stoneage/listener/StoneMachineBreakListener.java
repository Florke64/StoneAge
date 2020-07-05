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
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import win.flrque.g2p.stoneage.StoneAge;
import win.flrque.g2p.stoneage.gui.Window;
import win.flrque.g2p.stoneage.gui.WindowManager;

public class StoneMachineBreakListener implements Listener {

    private final StoneAge plugin;

    public StoneMachineBreakListener() {
        plugin = StoneAge.getPlugin(StoneAge.class);
    }

    @EventHandler
    public void onStoneMachineBreak(@NotNull BlockBreakEvent event) {
        if(event.isCancelled())
            return;

        if(event.getPlayer() == null)
            return;

        final Player destroyer = event.getPlayer();
        final GameMode gameMode = destroyer.getGameMode();

        if(!plugin.getStoneMachine().isStoneMachine(event.getBlock()))
            return;

        event.setDropItems(false);

        final ItemStack tool = destroyer.getInventory().getItemInMainHand();
        if(tool.getType() == Material.GOLD_PICKAXE || gameMode == GameMode.CREATIVE) {
            final Block brokenBlock = event.getBlock();
            final Location brokenBlockLocation = brokenBlock.getLocation();

            //Closing all active windows
            final WindowManager windowManager = plugin.getWindowManager();
            final Window brokenMachinesWindow = windowManager.getWindow((Dispenser) brokenBlock.getState());
            if(brokenMachinesWindow != null) {
                for (Player user : brokenMachinesWindow.getUsers()) {
                    if (user != null && user.isOnline()) {

                        //TODO: To be tested in action
                        if (user.getOpenInventory() == null)
                            continue;

                        if (windowManager.getWindow(user).getBukkitInventory().equals(user.getOpenInventory().getTopInventory())) {
                            user.closeInventory();
                            user.sendMessage("Ta stoniarka zostala zniszczona.");
                        }
                    }
                }
            }

            if(gameMode != GameMode.CREATIVE) {
                brokenBlockLocation.getWorld().dropItemNaturally(brokenBlockLocation, plugin.getStoneMachine().createStoneMachineItem());
            }

        } else {
            event.setCancelled(true);
            destroyer.sendMessage("Stoniarka moze byc usunieta tylko przy pomocy zlotego kilofa!");
        }
    }

}
