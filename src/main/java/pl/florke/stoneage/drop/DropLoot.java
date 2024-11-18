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

package pl.florke.stoneage.drop;

import org.bukkit.block.TileState;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import pl.florke.stoneage.StoneAge;
import pl.florke.stoneage.machine.ItemAutoSmelter;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/* Objects of this class are the "loot boxes" that drop, when machine used.
 * They may contain multiple different ItemStacks as well as experience.
 * Contents are calculated by DropCalculator and ExperienceCalculator respectively. */
public class DropLoot {

    private final ItemAutoSmelter itemSmelter = StoneAge.getPlugin(StoneAge.class).getStoneMachine().getItemSmelter();

    private final Map<DropEntry, ItemStack> loots = new HashMap<>();

    private int totalExp = 0;

    public void addLoot(@NotNull DropEntry dropEntry, ItemStack itemStack) {
        if (dropEntry.getMaximalExp() > 0)
            totalExp += dropEntry.calculateFinalExpValue();

        loots.put(dropEntry, itemStack.clone());
    }

    public ItemStack getItemLoot(DropEntry entry) {
        return loots.get(entry).clone();
    }

    public int getExp() {
        return totalExp;
    }

    public Collection<DropEntry> getActiveDropEntries() {
        return loots.keySet();
    }

    public int getAmountLooted(DropEntry key) {
        return loots.get(key).getAmount();
    }

    public void applyAutoSmeltingFeature(TileState machineState) {
        for (Map.Entry<DropEntry, ItemStack> entry : loots.entrySet()) {
            final ItemStack itemDrop = entry.getValue();

            //AUTO-SMELTING FEATURE
            if (machineState != null && itemSmelter.getAutoSmeltingUsesLeft(machineState) >= itemDrop.getAmount()) {
                final ItemStack smelted = itemSmelter.getSmelted(machineState, itemDrop);
                if (smelted != null)
                    loots.put(entry.getKey(), smelted);
            }
        }
    }

}
