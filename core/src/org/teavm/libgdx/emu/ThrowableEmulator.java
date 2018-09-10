package org.teavm.libgdx.emu;

import org.teavm.libgdx.plugin.Annotations.Emulate;

@Emulate(Throwable.class)
public class ThrowableEmulator{
    public Throwable getCause(){
        return null;
    }
}
