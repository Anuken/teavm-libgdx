package org.teavm.libgdx;

import com.badlogic.gdx.ApplicationLogger;
import org.teavm.jso.JSBody;

public class TeaVMApplicationLogger implements ApplicationLogger {

    @Override
    public void log (String tag, String message) {
        consoleLog("[" + tag + "] " + message);
    }

    @Override
    public void log (String tag, String message, Throwable exception) {
        consoleLog("[" + tag + "] " + message);
        exception.printStackTrace(System.out);
    }

    @Override
    public void error (String tag, String message) {
        consoleLog("[" + tag + "] " + message);
    }

    @Override
    public void error (String tag, String message, Throwable exception) {
        consoleLog("[" + tag + "] " + message);
        exception.printStackTrace(System.err);
    }

    @Override
    public void debug (String tag, String message) {
        consoleLog("[" + tag + "] " + message);
    }

    @Override
    public void debug (String tag, String message, Throwable exception) {
        consoleLog("[" + tag + "] " + message);
        exception.printStackTrace(System.out);
    }

    @JSBody(params = "message", script = "console.log(\"TeaVM: \" + message);")
    native static public void consoleLog(String message);
}
