/*
 * Copyright Go2Play.pl (c) 2020.
 * Program made for Go2Play Skyblock server. It's not allowed to re-distribute the code.
 * Author: FlrQue
 */

package win.flrque.g2p.stoneage.listener;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Dispenser;
import org.bukkit.block.Hopper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import win.flrque.g2p.stoneage.StoneAge;
import win.flrque.g2p.stoneage.database.playerdata.PlayerStats;
import win.flrque.g2p.stoneage.drop.DropEntry;
import win.flrque.g2p.stoneage.drop.DropLoot;
import win.flrque.g2p.stoneage.event.StoneDropLootEvent;
import win.flrque.g2p.stoneage.event.StoneMachineStoneBreakEvent;
import win.flrque.g2p.stoneage.util.Message;

import java.util.logging.Level;

public class StoneBreakListener implements Listener {

    private final StoneAge plugin;

    public StoneBreakListener() {
        plugin = StoneAge.getPlugin(StoneAge.class);
    }

    @EventHandler
    public void onStoneBreak(@NotNull BlockBreakEvent event) {
        if(event.isCancelled()) return;

//        long start = System.currentTimeMillis();

        final Player player = event.getPlayer();
        if(player == null) return;

        final Block brokenBlock = event.getBlock();
        if(!brokenBlock.getType().equals(Material.STONE)) return;

        if(plugin.getDropCalculator().getDropMultiplier().isActive())
            plugin.getMultiplierBossBar().addPlayer(player);

        @SuppressWarnings("deprecation")
        byte stoneType = brokenBlock.getState().getData().getData();
        if(stoneType != ((byte) 0)) return;

        final Block machineBlock = plugin.getStoneMachine().getConnectedStoneMachine(brokenBlock);
        final Dispenser stoneMachine = machineBlock != null ? (Dispenser) machineBlock.getState() : null;

        //Cancelling default drops
        event.setDropItems(false);

        customizeStoneDrop(player, stoneMachine, brokenBlock);

//        long stop = System.currentTimeMillis();
//        plugin.getLogger().log(Level.INFO, "StoneBreakListener took " + (stop-start) + "ms of main thread time.");

    }

    private void customizeStoneDrop(@NotNull Player player, Dispenser stoneMachine, Block brokenBlock) {
        final GameMode playerGameMode = player.getGameMode();
        final DropLoot finalDrop;
        if(playerGameMode != GameMode.CREATIVE && playerGameMode != GameMode.SPECTATOR) {
            final ItemStack usedTool = player.getInventory().getItemInMainHand();
            //TODO: Async calculation
            finalDrop = plugin.getDropCalculator().calculateDrop(player, usedTool, stoneMachine);

            if(finalDrop == null) {
                plugin.getLogger().log(Level.INFO, "DropLoot calculated is null (Player: "+ player.getName() +", Location: "+ player.getLocation().toString() +")");
            }

            dropLoot(player, brokenBlock.getLocation(), stoneMachine, finalDrop);
        } else {
            finalDrop = null;
        }

        if(stoneMachine != null && stoneMachine.getBlock() != null){
            final StoneMachineStoneBreakEvent stoneBreakEvent = new StoneMachineStoneBreakEvent(player, stoneMachine, finalDrop);
            Bukkit.getServer().getPluginManager().callEvent(stoneBreakEvent);

            //Replacing broken stone with new one
            new BukkitRunnable() {
                @Override
                public void run() {
                    plugin.getStoneMachine().generateStone(brokenBlock.getLocation());
                }
            }.runTaskLater(plugin, 1l);
        }
    }

    private void dropLoot(Player player, Location stoneLoc, @Nullable Dispenser stoneMachine, DropLoot dropLoot) {
        if(dropLoot == null) {
            return;
        }

        boolean hasHopper = false;
        Block blockUnderStoneMachine = null;
        //Verifying plugin's config and using hopper output if allowed
        if (stoneMachine != null && plugin.getStoneMachine().isHopperOutputAllowed()) {
            blockUnderStoneMachine = stoneMachine.getBlock().getRelative(BlockFace.DOWN);
            if (blockUnderStoneMachine != null && blockUnderStoneMachine.getState() instanceof Hopper) {
                hasHopper = true;
            }
        }

        //Checking drop location and verifying plugin configuration
        final Location expDropLocation = (plugin.getStoneMachine().isDropExpToFeet()) ? player.getLocation() : stoneLoc;
        final Location itemDropLocation = (plugin.getStoneMachine().isDropItemsToFeet()) ? player.getLocation() : stoneLoc;

        if (dropLoot.getExp() > 0) {
            final Entity orb = expDropLocation.getWorld().spawnEntity(expDropLocation, EntityType.EXPERIENCE_ORB);
            ((ExperienceOrb) orb).setExperience(dropLoot.getExp());
        }

        //Looping through all loots and dropping them for the player to pickup.
        for (DropEntry drop : dropLoot.getActiveDropEntries()) {
            final ItemStack itemLoot = dropLoot.getItemLoot(drop);
            if(itemLoot == null)
                continue;

            final int totalAmount = itemLoot.getAmount();

            //Calling API Event
            final StoneDropLootEvent lootEvent = new StoneDropLootEvent(player, itemLoot);
            Bukkit.getServer().getPluginManager().callEvent(lootEvent);

            if(lootEvent.isCancelled()) {
                continue;
            }

            final PlayerStats stats = this.plugin.getPlayerSetup().getPlayerStoneMachineStats(player.getUniqueId());
            stats.addMinerExp(drop.getMinerExp());

            //Drop to hopper under the Stone Machine
            ItemStack hopperLeftItem = null;
            if (blockUnderStoneMachine != null && hasHopper) {
                final Inventory hopperInventory = ((Hopper) blockUnderStoneMachine.getState()).getInventory();
                hopperLeftItem = addItemToInventory(itemLoot, hopperInventory);
            }

            //Drop under player's feet if there is no hopper under Stone Machine
            if (!hasHopper && drop != null && itemLoot != null) {
                itemDropLocation.getWorld().dropItemNaturally(itemDropLocation, itemLoot);
            }
            //Drop under player's feet in case if hopper cannot handle them all
            else if (hasHopper && hopperLeftItem != null) {
                itemDropLocation.getWorld().dropItemNaturally(itemDropLocation, hopperLeftItem);
            }

            if(drop != plugin.getDropCalculator().getPrimitiveDropEntry()) {
                final Message dropMessage = new Message("&7Udalo ci sie wykopac &c$_1 &7x&6$_2");
                dropMessage.setVariable(1, drop.getCustomName());
                dropMessage.setVariable(2, Integer.toString(totalAmount));
                dropMessage.sendActionMessage(player);
            }
        }
    }

    @Nullable
    private ItemStack addItemToInventory(@NotNull ItemStack itemStack, @NotNull Inventory inventory) {
        //Updating fist free slot index
        int firstFreeSlot = inventory.firstEmpty();
        if (firstFreeSlot != -1) {
            inventory.setItem(firstFreeSlot, itemStack);
            return null;
        }

        int slot = 0;
        int leftToAdd = itemStack.getAmount();
        for (ItemStack itemInInv : inventory.getContents()) {

            //Adding as much as possible to the Inventory
            final int maxStackSize = itemInInv.getMaxStackSize();
            if (itemInInv.isSimilar(itemStack) && itemInInv.getAmount() < maxStackSize) {
                final int handleSize = maxStackSize - itemInInv.getAmount();
                final int adding = itemStack.getAmount() < handleSize ? itemStack.getAmount() : handleSize;
                itemInInv.setAmount(itemInInv.getAmount() + ((adding > 0)? adding : 0));

                leftToAdd -= adding > 0? adding : 0;

                if(leftToAdd < 1) break;

            }

            slot++;
        }

        itemStack.setAmount(leftToAdd);
        return itemStack.getAmount() > 0? itemStack : null;

    }

}
