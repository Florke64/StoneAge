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
import pl.florke.stoneage.StoneAge;
import pl.florke.stoneage.database.playerdata.PlayerStats;
import pl.florke.stoneage.drop.DropEntry;
import pl.florke.stoneage.drop.DropLoot;
import pl.florke.stoneage.drop.DropMultiplier;
import pl.florke.stoneage.event.StoneDropLootEvent;
import pl.florke.stoneage.event.StoneMachineStoneBreakEvent;
import pl.florke.stoneage.util.Message;

import java.util.logging.Level;

public class StoneBreakListener implements Listener {

    private final StoneAge plugin;

    public StoneBreakListener() {
        plugin = StoneAge.getPlugin(StoneAge.class);
    }

    @EventHandler
    public void onStoneBreak(@NotNull BlockBreakEvent event) {
        if (event.isCancelled()) return;

        final Player player = event.getPlayer();

        final Block brokenBlock = event.getBlock();
        if (!brokenBlock.getType().equals(Material.STONE)) return;

        final DropMultiplier dropMultiplier = plugin.getDropCalculator().getDropMultiplier();
        if (dropMultiplier.isActive() && !dropMultiplier.getMultiplierBossBar().getPlayers().contains(player)) {
            dropMultiplier.getMultiplierBossBar().addPlayer(player);
            dropMultiplier.getMultiplierBossBar().setVisible(true);
        }

        final Block machineBlock = plugin.getStoneMachine().getConnectedStoneMachine(brokenBlock);
        final Dispenser stoneMachine = machineBlock != null ? (Dispenser) machineBlock.getState() : null;

        if (stoneMachine == null)
            return;

        //Cancelling default drops
        event.setDropItems(false);

        customizeStoneDrop(player, stoneMachine, brokenBlock);
    }

    private void customizeStoneDrop(@NotNull Player player, Dispenser stoneMachine, Block brokenBlock) {
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR)
            return;

        if (stoneMachine == null)
            return;

        stoneMachine.getBlock();

        final ItemStack usedTool = player.getInventory().getItemInMainHand();

        //Not applicable tool was used, means no drops
        if (!plugin.getApplicableTools().isApplicableTool(usedTool.getType()))
            new Message(plugin.getLanguage("stone-machine-drop-fail-tool")).send(player);

        new BukkitRunnable() {
            @Override
            public void run() {
                final DropLoot finalDrop;
                finalDrop = plugin.getDropCalculator().calculateDrop(player, usedTool, stoneMachine);

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        final StoneMachineStoneBreakEvent stoneBreakEvent = new StoneMachineStoneBreakEvent(player, stoneMachine, finalDrop);
                        Bukkit.getServer().getPluginManager().callEvent(stoneBreakEvent);

                        dropLoot(player, brokenBlock.getLocation(), stoneMachine, finalDrop);
                    }
                }.runTask(plugin);
            }
        }.runTaskAsynchronously(plugin);

        //Replacing broken stone with new one
        new BukkitRunnable() {
            @Override
            public void run() {
                plugin.getStoneMachine().generateStone(brokenBlock.getLocation());
            }
        }.runTaskLater(plugin, 1L);
    }

    private void dropLoot(Player player, Location stoneLoc, @Nullable Dispenser stoneMachine, DropLoot dropLoot) {
        if (dropLoot == null)
            return;

        boolean hasHopper = false;
        Block blockUnderStoneMachine = null;
        //Verifying plugin's config and using hopper output if allowed
        if (stoneMachine != null && plugin.getStoneMachine().isHopperOutputAllowed()) {
            blockUnderStoneMachine = stoneMachine.getBlock().getRelative(BlockFace.DOWN);
            if (blockUnderStoneMachine.getState() instanceof Hopper) {
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
            if (itemLoot == null)
                continue;

            final int totalAmount = itemLoot.getAmount();

            //Calling API Event
            final StoneDropLootEvent lootEvent = new StoneDropLootEvent(player, itemLoot);
            Bukkit.getServer().getPluginManager().callEvent(lootEvent);

            if (lootEvent.isCancelled()) {
                continue;
            }

            final PlayerStats stats = this.plugin.getPlayersData().getPlayerStoneMachineStats(player.getUniqueId());
            stats.addMinerExp(drop.getMinerExp());

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


            if (drop != plugin.getDropCalculator().getPrimitiveDropEntry()) {
                final Message dropMessage = new Message(plugin.getLanguage("stone-machine-drop-alert"));
                dropMessage.placeholder(1, drop.getCustomName());
                dropMessage.placeholder(2, Integer.toString(totalAmount));
                dropMessage.sendActionMessage(player);
            }
        }
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
            }

            if (itemInInv == null)
                new Message("StoneBreakListener: Hej! Null item in inventory! This is probably an error.").log(Level.SEVERE);

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
