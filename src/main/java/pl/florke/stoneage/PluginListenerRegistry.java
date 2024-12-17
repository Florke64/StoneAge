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

package pl.florke.stoneage;

import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import pl.florke.stoneage.util.Message;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;

public class PluginListenerRegistry {

    private final JavaPlugin plugin;

    private final List<Listener> listeners = new ArrayList<>();

    public PluginListenerRegistry(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void registerListener(Class<? extends Listener> listener) {
        try {
            final Constructor<?>[] listenerConstructors = listener.getConstructors();
            if (listenerConstructors.length < 1)
                return;

            final PluginManager pluginManager = plugin.getServer().getPluginManager();
            final Listener newListener = (Listener) listenerConstructors[0].newInstance();

            listeners.add(newListener);
            pluginManager.registerEvents(newListener, plugin);

        } catch (ReflectiveOperationException | SecurityException ex) {
            new Message("Cannot register listener: " + listener.getName(), ex.getMessage()).log(Level.SEVERE);
        }
    }
}
