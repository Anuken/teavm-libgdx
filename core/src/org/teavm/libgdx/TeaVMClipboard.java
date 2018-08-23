package org.teavm.libgdx;

import com.badlogic.gdx.utils.Clipboard;

public class TeaVMClipboard implements Clipboard {
    private String content = "";

    @Override
    public String getContents () {
        return content;
    }

    @Override
    public void setContents (String content) {
        this.content = content;
    }
}
