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

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import pl.florke.stoneage.drop.DropEntry;
import pl.florke.stoneage.util.Message;

import java.util.logging.Level;

public class DropEntryConfigReader extends ConfigSectionReader {

    public DropEntryConfigReader(ConfigurationSection section) {
        super(section);
    }

    public DropEntry readDropEntry(final DropEntry.EntryType type, final NamespacedKey key) {
        //Block that represents this drop (not dropping actually)
        final Material blockMaterial = readBlockMaterial();

        //Getting drop's chance weight
        final float weight = readDropChanceWeight();

        //Reading actual drops
        ItemStack defaultDrop = null;
        ItemStack silkDrop = null;

        //Reading drops for default tool
        final ConfigurationSection defaultDropSection = rootSection.getConfigurationSection("drop");
        if (defaultDropSection != null) {
            final ItemStackConfigReader defaultToolConfig = new ItemStackConfigReader(defaultDropSection);

            defaultDrop = defaultToolConfig.getItemStack();
        }

        //Reading drops for tools with Silk Touch enchantment
        final ConfigurationSection silkDropSection = rootSection.getConfigurationSection("silk");
        if (silkDropSection != null) {
            final ItemStackConfigReader silkToolConfig = new ItemStackConfigReader(silkDropSection);

            silkDrop = silkToolConfig.getItemStack();
        }

        if (rootSection.getConfigurationSection("silk") == null) {
            for (String s : rootSection.getKeys(false)) {
                new Message(s).log(Level.WARNING);
            }
        }

        //If there was an error while reading those sections
        if (defaultDrop == null && silkDrop == null) {
            new Message("Error while reading DropEntry: $_1")
                .placeholder(1, rootSection.getString("material", "<?>"))
                    .log(Level.SEVERE);

            return null;
        }

        defaultDrop = (defaultDrop != null) ? defaultDrop : silkDrop;

        final DropEntry dropEntry = new DropEntry(type, key, defaultDrop, weight);

        dropEntry.setBlockMaterial(blockMaterial == null? Material.STONE : blockMaterial);

        //Setting Silk Touch enchantment drop
        if (silkDrop != null)
            dropEntry.setSilkDrop(silkDrop);

        // TODO: Only use "set[...something...]();" methods if section exists
        //Getting DropEntry custom name for its display in menus
        final String customEntryName = rootSection.getString("custom_name", dropEntry.getBlockMaterial().name());
        dropEntry.setCustomName(customEntryName);

        //Fortune Enchant ignoring
        final boolean ignoreFortune = rootSection.getBoolean("ignore_fortune", true);
        dropEntry.setIgnoreFortune(ignoreFortune);

        //Accepts drop multiplication set by server admin
        final boolean multipliable = rootSection.getBoolean("multipliable", true);
        dropEntry.setMultipliable(multipliable);

        //Minimal and Maximal drop count
        final int minAmount = defaultDropSection.getInt("minimal_amount", 1);
        final int maxAmount = defaultDropSection.getInt("maximal_amount", 1);
        dropEntry.setMinAmount(minAmount);
        dropEntry.setMaxAmount(maxAmount);

        //Experience Drops
        final int minExp = rootSection.getInt("minimal_exp", 1);
        final int maxExp = rootSection.getInt("maximal_exp", 5);
        dropEntry.setMinimalExp(minExp);
        dropEntry.setMaximalExp(maxExp);

        //Mining Experience
        final int minerExp = rootSection.getInt("miner_exp", 0);
        dropEntry.setMinerExp(minerExp);

        //Needed level to drop
        final int neededMinerLevel = rootSection.getInt("miner_level", 1);
        dropEntry.setNeededMinerLevel(neededMinerLevel);

        return dropEntry;
    }

    private float readDropChanceWeight() {
        final String weightString = rootSection.getString("weight");
        if (weightString == null)
            return 0.0f;

        float weight;
        try {
            weight = Float.parseFloat(weightString);
        } catch (NumberFormatException ex) {
            weight = 0.0f;
            new Message("Null DropEntry weight: $_1").placeholder(1, weightString).log(Level.SEVERE);
        }

        return weight;
    }

    private Material readBlockMaterial() {
        final String materialName = rootSection.getString("block.material");
        if (materialName == null)
            return null;

        return Material.getMaterial(materialName);
    }

}
