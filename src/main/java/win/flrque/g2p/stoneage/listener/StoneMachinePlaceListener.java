package win.flrque.g2p.stoneage.listener;

import org.bukkit.block.Dispenser;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import win.flrque.g2p.stoneage.item.StoneMachine;

public class StoneMachinePlaceListener implements Listener {

    @EventHandler
    public void onStoneMachinePlace(BlockPlaceEvent event) {
        if(event.isCancelled())
            return;

        final Player player = event.getPlayer();

        final ItemStack stoneMachine = StoneMachine.getExampleStoneMachine();

        //Better safe than sorry :P
        if(stoneMachine.getItemMeta() == null)
            return;

        //Check if it's Dispenser.
        if(!(event.getBlockPlaced().getState() instanceof Dispenser))
            return;

        final Dispenser stoneMachineBlock = (Dispenser) event.getBlockPlaced().getState();

        //This isn't Stone Machine >:(
        if(stoneMachineBlock.getCustomName() == null)
            return;

        //Bloop! Checking custom names for match.
        if(stoneMachineBlock.getCustomName().equals(stoneMachine.getItemMeta().getDisplayName())) {
            player.sendMessage("<Edward> Twoja stoniarka postawiona, gz buddy!");
        }
    }

}
