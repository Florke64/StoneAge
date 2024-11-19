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

import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import pl.florke.stoneage.StoneAge;
import pl.florke.stoneage.drop.DropEntry;
import pl.florke.stoneage.util.Message;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

public class MachinesConfigReader {

    private final JavaPlugin plugin;

    public MachinesConfigReader(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public List<DropEntry> getCustomDropEntries() {
        final DropEntry.EntryType type = DropEntry.EntryType.CUSTOM_DROP;
        return readCustomDropEntriesDirectory(type);
    }

    public List<DropEntry> getDropResourceEntries() {
        final DropEntry.EntryType type = DropEntry.EntryType.RESOURCE_DROP;
        return readCustomDropEntriesDirectory(type);
    }

    private List<DropEntry> readCustomDropEntriesDirectory(DropEntry.EntryType types) {
        final File dropEntryDirectory = new File(plugin.getDataFolder(), types.getPath());
        final List<DropEntry> drops = new ArrayList<>();

        if (!dropEntryDirectory.exists())
            saveDefaultDrops();

        final File[] dropEntryFiles = dropEntryDirectory.listFiles();

        if (dropEntryFiles == null) {
            new Message("Unable to read drops directory!", dropEntryDirectory.getAbsolutePath()).log(Level.SEVERE);
            return drops;
        }

        return readCustomDropEntriesDirectory(types, dropEntryFiles);
    }

    private List<DropEntry> readCustomDropEntriesDirectory(DropEntry.EntryType types, File[] dropEntryFiles) {
        final List<DropEntry> drops = new ArrayList<>();
        if (dropEntryFiles == null)
            return drops;

        final int customDropsFound = (int) Arrays.stream(dropEntryFiles).filter(f ->
                f != null && f.exists() && f.isFile() && f.getAbsolutePath().endsWith(".yml")
        ).count();

        int customDropsLoaded = 0;
        for (File dropEntryFile : dropEntryFiles) {
            if (dropEntryFile == null || !dropEntryFile.getAbsolutePath().endsWith(".yml"))
                continue;

            new Message("Attempting to load drop entry: $_1")
                    .placeholder(1, dropEntryFile.getName().split("\\.yml", 2)[0]).log(Level.INFO);

            final DropEntry dropEntry = readDrop(types, dropEntryFile);
            if (dropEntry == null)
                continue;

            drops.add(dropEntry);

            customDropsLoaded++;
        }

        new Message("Loaded $_1 of $_2 custom drop entries.")
                .placeholder(1, Integer.toString(customDropsLoaded))
                .placeholder(2, Integer.toString(customDropsFound))
                .log(Level.INFO);

        return drops;
    }

    private DropEntry readDrop(DropEntry.EntryType type, File file) {
        if (file == null || !file.exists() || !file.isFile())
            return null;

        final YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);

        final DropEntryConfigReader customDropEntry = new DropEntryConfigReader(yaml);
        final String rawKey = file.getName().split("\\.yml", 2)[0];
        final NamespacedKey key = new NamespacedKey(StoneAge.getPlugin(StoneAge.class), type.getPrefix() + rawKey.toLowerCase());
        final DropEntry dropEntry = customDropEntry.readDropEntry(type, key);

        new Message(dropEntry == null ? "Failed to load a custom drop: $_1" : "Loaded a custom drop: $_1")
            .placeholder(1, file.getAbsolutePath()).log(Level.INFO);

        return dropEntry;
    }

    public void saveDefaultDrops() {
        final File customDropsDirectory = new File(plugin.getDataFolder(), DropEntry.EntryType.CUSTOM_DROP.getPath());
        final File resourcesDirectory = new File(plugin.getDataFolder(), DropEntry.EntryType.RESOURCE_DROP.getPath());

        if (customDropsDirectory.exists() || resourcesDirectory.exists())
            return;

        plugin.saveResource("drops/coal.yml", false);
        plugin.saveResource("drops/iron.yml", false);
        plugin.saveResource("drops/gold.yml", false);
        plugin.saveResource("drops/lapis.yml", false);
        plugin.saveResource("drops/redstone.yml", false);
        plugin.saveResource("drops/diamond.yml", false);
        plugin.saveResource("drops/mending.yml", false);

        plugin.saveResource("drops/resources/stone.yml", false);
        plugin.saveResource("drops/resources/stone_bricks.yml", false);
    }
}
