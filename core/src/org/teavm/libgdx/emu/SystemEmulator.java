package org.teavm.libgdx.emu;

import org.teavm.libgdx.plugin.Annotations.Emulate;

@Emulate(System.class)
public class SystemEmulator{

    public static String getenv(String var0) {
        return null;
    }
}
