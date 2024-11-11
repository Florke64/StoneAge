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

package pl.florke.stoneage.util;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import pl.florke.stoneage.StoneAge;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class Language {

    private final StoneAge stoneAge = StoneAge.getPlugin(StoneAge.class);
    private final String languageKey;

    private final Map<String, List<String>> translationLists = new HashMap<>(); // <key, translation>
    private final Map<String, String> translationMap = new HashMap<>();

    public Language(final String langKey) {
        this.languageKey = langKey;
    }

    /**
     * Retrieves the translated text associated with the given key.
     *
     * @param key the key for which the translation is requested
     * @return an array of strings representing the translation; if the key is not found in the
     * translation list, it returns a single-element array containing the default translation
     * or the key itself if no translation exists. Key collision may occur if the same key is
     * in `translationList` and `translationMap` at the same time. `translationList` takes
     * priority over `translationMap`. Language file loading throws warning to console if
     * collision is detected.
     * @see #readYamlLangModule(YamlConfiguration, String)
     */
    public String[] getText(final String key) {
        if (translationLists.containsKey(key)) {
            final List<String> list = translationLists.get(key);
            return list.toArray(new String[0]);
        }

        return new String[]{translationMap.getOrDefault(key, key)};
    }

    public void reload() {
        try {
            translationLists.clear();
        } catch (RuntimeException ex) {
            new Message("Failed to clear translation lists", ex.getMessage()).log(Level.SEVERE);
        }

        // Read language file
        File languageFile = null;
        try {
            languageFile = new File(stoneAge.getDataFolder(),languageKey + ".yml");
        } catch (RuntimeException ex) {
            new Message("Language file: " + languageKey + ".yml not found").log(Level.WARNING);
        }

        if (languageFile == null || !languageFile.exists()) {
            stoneAge.saveResource(languageKey + ".yml", false);
        }

        // Load language file
        loadYaml(languageFile);

        new Message("Language file reloaded.").log(Level.INFO);
    }

    private void loadYaml(final File file) {
        final YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);

        // Read language modules aka ConfigurationSections
        for (final String langModule : yaml.getKeys(false)) {
            new Message("Loading language module: " + langModule).log(Level.INFO);
            readYamlLangModule(yaml, langModule);
        }

        /* Modules were made to make language files more modular and easier to read. */
    }

    private void readYamlLangModule(final @NotNull YamlConfiguration yaml, final String langModule) {
        final ConfigurationSection section = yaml.getConfigurationSection(langModule);
        if (section == null) return;

        for (final String key : section.getKeys(false)) {
            if (section.isList(key))
                translationLists.put(key, section.getStringList(key));
            else
                translationMap.put(key, section.getString(key));

            if (isCollision(key))
                collisionWarning(key);
        }
    }

    private boolean isCollision(final String key) {
        return translationLists.containsKey(key) && translationMap.containsKey(key);
    }

    private void collisionWarning(final String key) {
        new Message("Language key collision detected: `" + key + "`. Check your lang.yml!")
                .placeholder(0, key).log(Level.WARNING);
    }

}
