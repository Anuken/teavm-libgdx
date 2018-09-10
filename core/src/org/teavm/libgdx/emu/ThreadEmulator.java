package org.teavm.libgdx.emu;

import org.teavm.libgdx.plugin.Annotations.Emulate;

@Emulate(Thread.class)
public class ThreadEmulator{
    public void setDaemon(boolean b){}
}
