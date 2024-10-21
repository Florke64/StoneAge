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

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class DropLoot {

    private final Map<DropEntry, ItemStack> loots = new HashMap<>();

    private int totalExp = 0;

    public void addLoot(@NotNull DropEntry dropEntry, ItemStack itemStack) {
        if (dropEntry.getMaximalExp() > 0)
            totalExp += dropEntry.calculateFinalExpValue();

        loots.put(dropEntry, itemStack);
    }

    public ItemStack getItemLoot(DropEntry entry) {
        return loots.get(entry);
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

}
