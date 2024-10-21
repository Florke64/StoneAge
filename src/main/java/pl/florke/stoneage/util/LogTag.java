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

import org.jetbrains.annotations.NotNull;

public enum LogTag {

    START_UP("start-up"),
    CONFIG("config"),
    AUTO_SAVE("auto-save"),
    DATABASE("database"),
    DEBUG("debug"),
    ;

    private final String logSignature;

    LogTag(@NotNull final String signature) {
        this.logSignature = signature.toLowerCase();
    }

    @Override
    public String toString() {
        return this.logSignature;
    }
}
