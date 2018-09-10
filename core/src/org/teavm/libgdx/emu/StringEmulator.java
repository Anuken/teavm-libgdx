package org.teavm.libgdx.emu;

import org.teavm.libgdx.plugin.Annotations.Emulate;

@Emulate(String.class)
public abstract class StringEmulator implements CharSequence{

    public boolean contains(CharSequence other){
        outer:
        for(int i = 0; i < length() - other.length(); i++){
            for(int j = 0; j < other.length(); j++){
                if(charAt(i) != other.charAt(j)){
                    continue outer;
                }
            }
            return true;
        }
        return false;
    }
}
