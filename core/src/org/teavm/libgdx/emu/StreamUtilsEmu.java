package org.teavm.libgdx.emu;

import com.badlogic.gdx.utils.StreamUtils;
import org.teavm.libgdx.plugin.Annotations.Emulate;

import java.io.*;

@Emulate(StreamUtils.class)
public class StreamUtilsEmu{

    public static void closeQuietly (Closeable c) {

    }
}
