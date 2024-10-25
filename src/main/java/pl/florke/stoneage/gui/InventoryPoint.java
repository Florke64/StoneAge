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

package pl.florke.stoneage.gui;

import org.bukkit.event.inventory.InventoryType;
import pl.florke.stoneage.StoneAge;

/**
 * This object allows to easily convert raw slow value to
 * the Array of {x,y} and vice-versa.
 */
public class InventoryPoint {

    private final StoneAge plugin;

    private final InventoryType inventoryType;

    private int slotNumber;
    private final int[] point = new int[2];

    public InventoryPoint(InventoryType usedFrame, int slotNumber) {
        this.plugin = StoneAge.getPlugin(StoneAge.class);

        this.inventoryType = usedFrame;
        this.slotNumber = slotNumber;

        calculateSlotToPoint();
    }

    public InventoryPoint(InventoryType usedFrame, int x, int y) {
        this.plugin = StoneAge.getPlugin(StoneAge.class);

        this.inventoryType = usedFrame;

        this.point[0] = x;
        this.point[1] = y;

        calculatePointToSlot();
    }

    private void calculateSlotToPoint() {
        double slotNumber = this.slotNumber;

        switch (inventoryType) {
            case CHEST:
            case SHULKER_BOX:
            case ENDER_CHEST:
                this.point[0] = (this.slotNumber) % 9;
                this.point[1] = (int) Math.floor((slotNumber / 9));
                break;
        }
    }

    private void calculatePointToSlot() {
        switch (inventoryType) {
            case CHEST:
            case SHULKER_BOX:
            case ENDER_CHEST:
                this.slotNumber = this.point[0] + (9 * this.point[1]);
                break;
        }
    }

    /**
     * @return Summary of all slots till specified one.
     * Starting from number 0. Counting all slots from right to left
     */
    public int getSlotNumber() {
        return this.slotNumber;
    }

    /**
     * @return Int Array in length of 2 values. First value represents
     * X property and second value in array, stands for Y value.
     * Both X and Y are counted starting from number 0.
     * Higher X values are on the right side of GUI Window.
     * Higher Y values are on the bottom of GUI Window.
     */
    public int[] getPoint() {
        return this.point;
    }

    @Override
    public String toString() {
        return"InventoryPoint#toString(): " + slotNumber + ", " +
                " [" + point[0] + "; " + point[1] + "]";
    }
}
