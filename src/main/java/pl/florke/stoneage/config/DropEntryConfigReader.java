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

package pl.florke.stoneage.config;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import pl.florke.stoneage.drop.DropEntry;
import pl.florke.stoneage.util.Message;

import java.util.logging.Level;

public class DropEntryConfigReader extends ConfigSectionReader {

    public DropEntryConfigReader(ConfigurationSection section) {
        super(section);
    }

    public DropEntry compileDropEntry() {
        //Getting drop's chance weight
        final float weight = readDropChanceWeight();

        //Reading actual drops
        ItemStack defaultToolItem = null;
        ItemStack silkToolItem = null;

        final String canonicalEntryName = rootSection.getName();

        //Reading drops for default tool
        final ConfigurationSection defaultToolSection = rootSection.getConfigurationSection("default_tool");
        if (defaultToolSection != null) {
            final ItemStackConfigReader defaultToolConfig = new ItemStackConfigReader(defaultToolSection);

            defaultToolItem = defaultToolConfig.getItemStack();
        }

        //Reading drops for tools with Silk Touch enchantment
        final ConfigurationSection silkToolSection = rootSection.getConfigurationSection("silk_touch_tool");
        if (silkToolSection != null) {
            final ItemStackConfigReader silkToolConfig = new ItemStackConfigReader(silkToolSection);

            silkToolItem = silkToolConfig.getItemStack();
        }

        //If there was an error while reading those sections
        if (defaultToolItem == null && silkToolItem == null) {
            new Message("Error while reading DropEntry: $_1.")
                    .placeholder(1, rootSection.getName())
                    .log(Level.SEVERE);

            return null;
        }

        defaultToolItem = (defaultToolItem != null) ? defaultToolItem : silkToolItem;
        final DropEntry dropEntry = new DropEntry(canonicalEntryName, defaultToolItem, weight);

        //Setting Silk Touch enchantment drop
        if (silkToolItem != null)
            dropEntry.setSilkTouchItemStack(silkToolItem);

        //Fortune Enchant ignoring
        final String customEntryName = rootSection.getString("custom_name", canonicalEntryName);
        dropEntry.setCustomName(customEntryName);

        //Fortune Enchant ignoring
        final boolean ignoreFortune = rootSection.getBoolean("ignore_fortune", true);
        dropEntry.setIgnoreFortuneEnchant(ignoreFortune);

        //Accepts drop multiplication set by server admin
        boolean multipliable = rootSection.getBoolean("multipliable", true);
        dropEntry.setMultipliable(multipliable);

        //Minimal and Maximal drop count
        final int minAmount = defaultToolSection.getInt("minimal_amount", -1);
        final int maxAmount = defaultToolSection.getInt("maximal_amount", -1);
        dropEntry.setMinAmount(minAmount);
        dropEntry.setMaxAmount(maxAmount);

        //Experience Drops
        final int minExp = rootSection.getInt("minimal_exp", -1);
        final int maxExp = rootSection.getInt("maximal_exp", -1);
        dropEntry.setMinimalExp(minExp);
        dropEntry.setMaximalExp(maxExp);

        //Mining Experience
        final int minerExp = rootSection.getInt("miner_exp", 0);
        dropEntry.setMinerExp(minerExp);

        //Needed level to drop
        final int neededMinerLevel = rootSection.getInt("minimal_miner_lvl", 1);
        dropEntry.setNeededMinerLevel(neededMinerLevel);

        //Needed level to drop
        final int neededToolLevel = rootSection.getInt("minimal_tool_lvl", 1);
        dropEntry.setNeededToolLevel(neededToolLevel);

        return dropEntry;
    }

    private float readDropChanceWeight() {
        final String weightString = rootSection.getString("weight");
        if (weightString == null) {
            new Message("Error while reading DropEntry: null").log(Level.SEVERE);
            return 0.0f;
        }

        float weight;
        try {
            weight = Float.parseFloat(weightString);
        } catch (NumberFormatException ex) {
            weight = 0.0f;
            new Message("Null DropEntry weight: $_1").placeholder(1, weightString).log(Level.SEVERE);
        }

        return weight;
    }

}