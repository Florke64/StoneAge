/*
 * Copyright Go2Play.pl (c) 2020.
 * Program made for Go2Play Skyblock server. It's not allowed to re-distribute the code.
 * Author: FlrQue
 */

package win.flrque.g2p.stoneage.gui;

import org.bukkit.event.inventory.InventoryType;
import win.flrque.g2p.stoneage.StoneAge;

import java.util.logging.Logger;

/**
 * This object allows to easily convert raw slow value to
 * the Array of {x,y} and vice-versa.
 */
public class InventoryPoint {

    private final StoneAge plugin;
    private final Logger logger;

    private final InventoryType inventoryType;

    private int slotNumber;
    private int[] point = new int[2];

    public InventoryPoint(InventoryType usedFrame, int slotNumber) {
        this.plugin = StoneAge.getPlugin(StoneAge.class);
        this.logger = plugin.getLogger();

        this.inventoryType = usedFrame;
        this.slotNumber = slotNumber;

        calculateSlotToPoint();
    }

    public InventoryPoint(InventoryType usedFrame, int x, int y) {
        this.plugin = StoneAge.getPlugin(StoneAge.class);
        this.logger = plugin.getLogger();

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
        final StringBuilder text = new StringBuilder();
        text.append("InventoryPoint#toString(): ").append(slotNumber).append(", ");
        text.append(" [").append(point[0]).append("; ").append(point[1]).append("]");

        return text.toString();
    }
}
