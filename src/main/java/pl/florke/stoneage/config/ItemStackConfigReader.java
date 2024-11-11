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

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.florke.stoneage.util.Message;

import java.util.*;
import java.util.logging.Level;

public class ItemStackConfigReader extends ConfigSectionReader {

    private ItemStack cachedItemStack;

    public ItemStackConfigReader(ConfigurationSection section) {
        super(section);
    }

    public ItemStack getItemStack() {
        if (cachedItemStack == null)
            try {
                cachedItemStack = compileItemStack();
            } catch (InvalidConfigurationException ex) {
                new Message("Invalid Configuration of ItemStack");
            }

        return cachedItemStack;
    }

    public ItemStack compileItemStack() throws InvalidConfigurationException {
        //Getting material type from config section
        final Material itemMaterial = readMaterial();
        if (itemMaterial == null)
            throw new InvalidConfigurationException("Invalid Material name. Please refer to Bukkit's Material enum class.");

        final ItemStack itemStack = new ItemStack(itemMaterial);

        final ItemMeta itemMeta = itemStack.getItemMeta();

        //Getting item's custom name
        final Component customName = readCustomName();
        if (customName != null)
            itemMeta.displayName(customName);

        //Reading item's lore
        final Message lore = readLore();
        itemMeta.lore(Arrays.asList(lore.asComponents()));

        //Reading enchantments
        final Map<Enchantment, Integer> enchants = readEnchantments();
        for (Map.Entry<Enchantment, Integer> enchantment : enchants.entrySet())
            itemMeta.addEnchant(enchantment.getKey(), enchantment.getValue(), true);

        //Warping-ip the ItemStack
        itemStack.setItemMeta(itemMeta);

        return itemStack;
    }

    @Nullable
    private Material readMaterial() {
        final String materialName = rootSection.getString("material");
        if (materialName == null)
            return null;

        return Material.getMaterial(materialName.toUpperCase());
    }

    @Nullable
    private Component readCustomName() {
        final String customName = rootSection.getString("custom_name");
        if (customName == null)
            return null;

        return Component.text(customName);
    }

    @NotNull
    private Message readLore() {
        return new Message(rootSection.getStringList("lore"));
    }

    private Map<Enchantment, Integer> readEnchantments() {
        final RegistryAccess registryAccess = RegistryAccess.registryAccess();

        final List<String> enchantmentList = rootSection.getStringList("enchantments");
        final Map<Enchantment, Integer> enchantments = new HashMap<>();

        for (String enchant : enchantmentList) {
            // In config you declare: "enchant_name level"
            final String[] enchantmentData = enchant.split(" ", 2);

            final NamespacedKey enchantmentNamespacedKey = NamespacedKey.minecraft(enchantmentData[0].toLowerCase());

            Enchantment enchantment;
            int enchantmentLevel;

            final Registry<Enchantment> registry;
            try {
                registry = registryAccess.getRegistry(RegistryKey.ENCHANTMENT);

                enchantment = registry.get(enchantmentNamespacedKey);
                enchantmentLevel = (enchantmentData.length < 2) ? 1 : Integer.parseInt(enchantmentData[1]);

                enchantments.put(enchantment, enchantmentLevel);
            } catch (NoSuchElementException ex) {
                new Message("Invalid Enchantment for the item found - please double check the config.yml file!")
                        .log(Level.WARNING);
            }
        }

        return enchantments;
    }
}
