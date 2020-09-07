/*
 * Copyright Go2Play.pl (c) 2020.
 * Program made for Go2Play Skyblock server. It's not allowed to re-distribute the code.
 * Author: FlrQue
 */

package win.flrque.g2p.stoneage.util;

import org.jetbrains.annotations.NotNull;

public enum LogTag {

    START_UP("start-up"),
    CONFIG("config"),
    AUTO_SAVE("auto-save"),
    DATABASE("database"),
    DEBUG("debug"),;

    private final String logSignature;

    LogTag(@NotNull final String signature) {
        this.logSignature = signature.toLowerCase();
    }

    @Override
    public String toString() {
        return this.logSignature;
    }
}
