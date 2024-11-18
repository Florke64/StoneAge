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

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
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

    public List<DropEntry> getDropEntries() {
        return readDropEntriesDirectory(new File(plugin.getDataFolder(), "drops"), true);
    }

    public List<DropEntry> getPrimitiveDropEntries() {
        return readDropEntriesDirectory(new File(plugin.getDataFolder(), "drops/primitives"), true);
    }

    private List<DropEntry> readDropEntriesDirectory(File dropEntryDirectory, boolean saveDefault) {
        final List<DropEntry> drops = new ArrayList<>();

        if (saveDefault && !dropEntryDirectory.exists())
            saveDefaultDrops();

        final File[] dropEntryFiles = dropEntryDirectory.listFiles();

        if (dropEntryFiles == null) {
            new Message("Unable to read drops directory!", dropEntryDirectory.getAbsolutePath()).log(Level.SEVERE);
            return drops;
        }

        return readDropEntriesDirectory(dropEntryFiles);
    }

    private List<DropEntry> readDropEntriesDirectory(File[] dropEntryFiles) {
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

            final DropEntry dropEntry = readDrop(dropEntryFile);
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

    private DropEntry readDrop(File file) {
        if (file == null || !file.exists() || !file.isFile())
            return null;

        final YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);

        final DropEntryConfigReader customDropEntry = new DropEntryConfigReader(yaml);
        final DropEntry dropEntry = customDropEntry.readDropEntry(file.getName().split("\\.yml", 2)[0]);

        new Message(dropEntry == null ? "Failed to load a custom drop: $_1" : "Loaded a custom drop: $_1")
            .placeholder(1, file.getAbsolutePath()).log(Level.INFO);

        return dropEntry;
    }

    public void saveDefaultDrops() {
        final File customDropsDirectory = new File(plugin.getDataFolder(), "drops");
        if (!customDropsDirectory.exists()) {
            plugin.saveResource("drops/coal.yml", false);
            plugin.saveResource("drops/iron.yml", false);
            plugin.saveResource("drops/gold.yml", false);
            plugin.saveResource("drops/lapis.yml", false);
            plugin.saveResource("drops/redstone.yml", false);
            plugin.saveResource("drops/diamond.yml", false);
            plugin.saveResource("drops/mending.yml", false);
        }

        final File primitivesDirectory = new File(customDropsDirectory, "primitives");
        if (!primitivesDirectory.exists())
            plugin.saveResource("drops/primitives/stone.yml", false);

    }
}