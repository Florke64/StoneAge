/*
 * Copyright Go2Play.pl (c) 2020.
 * Program made for Go2Play Skyblock server. It's not allowed to re-distribute the code.
 * Author: FlrQue
 */

package win.flrque.g2p.stoneage.listener;

import org.bukkit.GameMode;
import org.bukkit.Location;
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
import win.flrque.g2p.stoneage.util.Message;

public class StoneMachineBreakListener implements Listener {

    private final StoneAge plugin;

    public StoneMachineBreakListener() {
        plugin = StoneAge.getPlugin(StoneAge.class);
    }

    @EventHandler
    public void onStoneMachineBreak(@NotNull BlockBreakEvent event) {
        if (event.isCancelled())
            return;

        if (event.getPlayer() == null)
            return;

        final Player destroyer = event.getPlayer();
        final GameMode gameMode = destroyer.getGameMode();

        if (!plugin.getStoneMachine().isStoneMachine(event.getBlock()))
            return;

        final Block brokenBlock = event.getBlock();
        final Dispenser stoneMachine = (Dispenser) brokenBlock.getState();

        event.setDropItems(false);

        final ItemStack tool = destroyer.getInventory().getItemInMainHand();
        if (plugin.getApplicableTools().isMachineDestroyTool(tool) || gameMode == GameMode.CREATIVE) {
            final Location brokenBlockLocation = brokenBlock.getLocation();

            stoneMachine.getInventory().clear();

            //Closing all active windows
            final WindowManager windowManager = plugin.getWindowManager();
            final Window brokenMachinesWindow = windowManager.getWindow(stoneMachine);
            if (brokenMachinesWindow != null) {

                final Message machineDestroyedMsg = new Message("&cTa stoniarka zostala zniszczona.");
                for (Player user : brokenMachinesWindow.getUsers()) {
                    if (user != null && user.isOnline()) {

                        //TODO: To be tested in action
                        if (user.getOpenInventory() == null)
                            continue;

                        if (windowManager.getWindow(user).getBukkitInventory().equals(user.getOpenInventory().getTopInventory())) {
                            user.closeInventory();
                            machineDestroyedMsg.send(destroyer);
                        }
                    }
                }
            }

            if (gameMode != GameMode.CREATIVE) {
                brokenBlockLocation.getWorld().dropItemNaturally(brokenBlockLocation, plugin.getStoneMachine().createStoneMachineItem());
            }

        } else {
            event.setCancelled(true);

            final Message wrongToolMsg = new Message("&7Stoniarka moze byc usunieta tylko przy pomocy &czlotego kilofa&7!");
            wrongToolMsg.send(destroyer);
        }
    }

}
