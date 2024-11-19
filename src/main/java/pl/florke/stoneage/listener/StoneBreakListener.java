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

package pl.florke.stoneage.listener;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Hopper;
import org.bukkit.block.TileState;
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
import pl.florke.stoneage.StoneAge;
import pl.florke.stoneage.database.playerdata.PlayerStats;
import pl.florke.stoneage.drop.DropEntry;
import pl.florke.stoneage.drop.DropLoot;
import pl.florke.stoneage.drop.DropMultiplier;
import pl.florke.stoneage.event.StoneDropLootEvent;
import pl.florke.stoneage.event.StoneMachineStoneBreakEvent;
import pl.florke.stoneage.util.Message;

public class StoneBreakListener implements Listener {

    private final StoneAge plugin;

    public StoneBreakListener() {
        plugin = StoneAge.getPlugin(StoneAge.class);
    }

    @EventHandler
    public void onStoneBreak(@NotNull BlockBreakEvent event) {
        if (event.isCancelled())
            return;

        final Player player = event.getPlayer();

        final Block brokenBlock = event.getBlock();
        if (!plugin.getDropCalculator().isDropResource(brokenBlock.getType()))
            return;

        final DropMultiplier dropMultiplier = plugin.getDropCalculator().getDropMultiplier();
        if (dropMultiplier.isActive() && !dropMultiplier.getMultiplierBossBar().getPlayers().contains(player)) {
            dropMultiplier.getMultiplierBossBar().addPlayer(player);
            dropMultiplier.getMultiplierBossBar().setVisible(true);
        }

        final Block machineBlock = plugin.getStoneMachine().getConnectedStoneMachine(brokenBlock);
        final Block resourceBlock = event.getBlock();
        final TileState stoneMachine = machineBlock != null ? (TileState) machineBlock.getState() : null;

        if (stoneMachine == null)
            return;

        customizeStoneDrop(player, stoneMachine, brokenBlock);

        //Cancelling default drops
        event.setExpToDrop(0);
        event.setDropItems(false);

        new BukkitRunnable() {
            @Override
            public void run() {

                resourceBlock.setType(Material.AIR);
            }
        }.runTaskLater(plugin, 1L);
    }

    private void customizeStoneDrop(@NotNull Player player, TileState machineState, Block brokenBlock) {
        if (machineState == null)
            return;

        player.sendBlockChange(brokenBlock.getLocation(), Material.AIR.createBlockData());

        // Respawn resource
        plugin.getStoneMachine().getResourceSpawner().spawnResource(brokenBlock.getLocation());

        final ItemStack usedTool = player.getInventory().getItemInMainHand();
        //Not applicable tool was used, means no drops
        if (!player.getGameMode().isInvulnerable() && brokenBlock.getDrops(usedTool, player).isEmpty()) {
            new Message(plugin.getLanguage("stone-machine-drop-fail-tool")).send(player);
            return;
        }

        final Material brokenMaterial = brokenBlock.getType();

        new BukkitRunnable() {
            @Override
            public void run() {
                if (player.getGameMode().isInvulnerable())
                    return;

                final DropLoot finalDrop = plugin.getDropCalculator().calculateCustomDrop(player, usedTool, machineState, brokenMaterial);

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        final StoneMachineStoneBreakEvent stoneBreakEvent = new StoneMachineStoneBreakEvent(player, machineState, finalDrop);
                        Bukkit.getServer().getPluginManager().callEvent(stoneBreakEvent);

                        dropLoot(player, brokenBlock.getLocation(), machineState, finalDrop);
                    }
                }.runTask(plugin);
            }
        }.runTaskAsynchronously(plugin);
    }

    private void dropLoot(Player player, Location stoneLocation, TileState machineState, DropLoot dropLoot) {
        if (dropLoot == null)
            return;

        //Checking drop location and verifying plugin configuration
        final Location expDropLocation = (plugin.getStoneMachine().isDropExpToFeet()) ? player.getLocation() : stoneLocation;

        if (dropLoot.getExp() > 0) {
            final Entity orb = expDropLocation.getWorld().spawnEntity(expDropLocation, EntityType.EXPERIENCE_ORB);
            ((ExperienceOrb) orb).setExperience(dropLoot.getExp());
        }

        //Looping through all loots and dropping them for the player to pickup.
        for (DropEntry drop : dropLoot.getActiveDropEntries()) {
            final ItemStack itemLoot = dropLoot.getItemLoot(drop);
            if (itemLoot == null)
                continue;

            final int totalAmount = itemLoot.getAmount();

            //Calling API Event
            final StoneDropLootEvent lootEvent = new StoneDropLootEvent(player, itemLoot);
            Bukkit.getServer().getPluginManager().callEvent(lootEvent);

            if (lootEvent.isCancelled())
                continue;

            //Drop to player
            dropLootToPlayer(machineState, itemLoot, player, stoneLocation);

            final PlayerStats stats = this.plugin.getPlayersData().getPlayerStoneMachineStats(player.getUniqueId());
            stats.addMinerExp(drop.getMinerExp());

            if (!plugin.getDropCalculator().isDropResource(drop)) {
                new Message(plugin.getLanguage("stone-machine-drop-alert"))
                    .placeholder(1, Message.constNamePrettify(drop.getCustomName()))
                    .placeholder(2, Integer.toString(totalAmount))
                        .sendActionMessage(player);
            }
        }
    }

    private void dropLootToPlayer(TileState machineState, ItemStack itemLoot, Player player, Location naturalDropLocation) {
        final Location itemDropLocation = plugin.getStoneMachine().isDropItemsToFeet()?
                player.getLocation() : naturalDropLocation;

        boolean hasHopper = false;
        Block blockUnderStoneMachine = null;
        //Verifying plugin's config and using hopper output if allowed
        if (machineState != null && plugin.getStoneMachine().isHopperOutputAllowed()) {
            blockUnderStoneMachine = machineState.getBlock().getRelative(BlockFace.DOWN);
            if (blockUnderStoneMachine.getState() instanceof Hopper) {
                hasHopper = true;
            }
        }

        //Drop to hopper under the Stone Machine
        ItemStack hopperLeftItem = null;
        if (blockUnderStoneMachine != null && hasHopper) {
            final Inventory hopperInventory = ((Hopper) blockUnderStoneMachine.getState()).getInventory();
            hopperLeftItem = addItemToInventory(itemLoot, hopperInventory);
        }

        //Drop under player's feet if there is no hopper under Stone Machine
        if (!hasHopper)
            itemDropLocation.getWorld().dropItemNaturally(itemDropLocation, itemLoot);

            //Drop under player's feet in case if hopper cannot handle them all
        else if (hopperLeftItem != null)
            itemDropLocation.getWorld().dropItemNaturally(itemDropLocation, hopperLeftItem);

    }

    @Nullable
    private ItemStack addItemToInventory(@NotNull ItemStack itemStack, @NotNull Inventory inventory) {
        int leftToAdd = itemStack.getAmount();
        for (ItemStack itemInInv : inventory.getContents()) {

            if (itemInInv == null) {
                //Updating fist free slot index
                int firstFreeSlot = inventory.firstEmpty();
                if (firstFreeSlot != -1) {
                    inventory.setItem(firstFreeSlot, itemStack);
                    return null;
                }
                return itemStack.getAmount() > 0 ? itemStack : null;
            }

            //Adding as much as possible to the Inventory
            final int maxStackSize = itemInInv.getMaxStackSize();
            if (itemInInv.isSimilar(itemStack) && itemInInv.getAmount() < maxStackSize) {
                final int handleSize = maxStackSize - itemInInv.getAmount();
                final int adding = Math.min(itemStack.getAmount(), handleSize);
                itemInInv.setAmount(itemInInv.getAmount() + (Math.max(adding, 0)));

                leftToAdd -= Math.max(adding, 0);

                if (leftToAdd < 1) break;
            }
        }

        itemStack.setAmount(leftToAdd);

        //Updating fist free slot index
        int firstFreeSlot = inventory.firstEmpty();
        if (firstFreeSlot != -1) {
            inventory.setItem(firstFreeSlot, itemStack);
            return null;
        }

        return itemStack.getAmount() > 0 ? itemStack : null;

    }

}
