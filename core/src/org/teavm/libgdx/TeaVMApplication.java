package org.teavm.libgdx;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.*;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import org.teavm.jso.browser.TimerHandler;
import org.teavm.jso.browser.Window;
import org.teavm.jso.dom.html.HTMLCanvasElement;
import org.teavm.jso.JSBody;
import com.badlogic.gdx.utils.Clipboard;

public class TeaVMApplication implements Application {
    private Window window = Window.current();
    private ApplicationListener listener;
    private TeaVMApplicationConfig config;
    private HTMLCanvasElement canvas;
    private TeaVMGraphics graphics;
    private TeaVMFiles files;
    private TeaVMAudio audio;
    private TeaVMInput input;
    private TeaVMNet net;
    private TeaVMClipboard clipboard;
    private int logLevel = LOG_ERROR;
    private Array<Runnable> runnables = new Array<>();
    private Array<LifecycleListener> lifecycleListeners = new Array<>();
    private ObjectMap<String, Preferences> prefs = new ObjectMap<>();
    private ApplicationLogger logger;
    private int lastWidth = -1, lastHeight = 1;

    public TeaVMApplication(ApplicationListener listener, TeaVMApplicationConfig config) {
        this.listener = listener;
        this.config = config;
    }

    public void start() {
        TeaVMFileLoader.loadFiles(new TeaVMFilePreloadListener() {
            @Override
            public void error() {
            }
            @Override
            public void complete() {
                startGdx();
            }
        });
    }

    private void startGdx() {
        canvas = config.getCanvas();
        graphics = new TeaVMGraphics(canvas, config);
        files = new TeaVMFiles();
        audio = new TeaVMAudio();
        input = new TeaVMInput(canvas);
        net = new TeaVMNet();
        logger = new TeaVMApplicationLogger();
        clipboard = new TeaVMClipboard();
        Gdx.app = this;
        Gdx.graphics = graphics;
        Gdx.gl = graphics.getGL20();
        Gdx.gl20 = graphics.getGL20();
        Gdx.files = files;
        Gdx.net = net;
        Gdx.audio = audio;
        Gdx.input = input;
        listener.create();
        listener.resize(canvas.getWidth(), canvas.getHeight());
        delayedStep();
    }

    private void delayedStep() {
        window.setTimeout(() -> step(), 10);
    }

    private void step() {
        graphics.update();
        graphics.frameId++;

        for (Runnable runnable : runnables) {
            runnable.run();
        }

        runnables.clear();
        if(lastWidth != canvas.getWidth() || lastHeight != canvas.getHeight()) {
            listener.resize(canvas.getWidth(), canvas.getHeight());
            lastWidth = canvas.getWidth();
            lastHeight = canvas.getHeight();
        }
        listener.render();
        input.reset();
        delayedStep();
    }

    @Override
    public ApplicationListener getApplicationListener() {
        return listener;
    }

    @Override
    public Graphics getGraphics() {
        return graphics;
    }

    @Override
    public Audio getAudio() {
        return audio;
    }

    @Override
    public Input getInput() {
        return input;
    }

    @Override
    public Files getFiles() {
        return files;
    }

    @Override
    public Net getNet() {
        return net;
    }

    @Override
    public void setApplicationLogger(ApplicationLogger applicationLogger) {
        this.logger = logger;
    }

    @Override
    public ApplicationLogger getApplicationLogger() {
        return logger;
    }

    @Override
    public void log(String tag, String message) {
        if (logLevel > LOG_INFO) logger.log(tag, message);
    }

    @Override
    public void log(String tag, String message, Throwable exception) {
        if (logLevel > LOG_INFO) logger.log(tag, message, exception);
    }

    @Override
    public void error(String tag, String message) {
        if (logLevel > LOG_ERROR) logger.error(tag, message);
    }

    @Override
    public void error(String tag, String message, Throwable exception) {
        if (logLevel > LOG_ERROR) logger.error(tag, message, exception);
    }

    @Override
    public void debug(String tag, String message) {
        if (logLevel >= LOG_DEBUG) logger.debug(tag, message);
    }

    @Override
    public void debug(String tag, String message, Throwable exception) {
        if (logLevel > LOG_DEBUG) logger.debug(tag, message, exception);
    }

    @Override
    public void setLogLevel(int logLevel) {
        this.logLevel = logLevel;
    }

    @Override
    public int getLogLevel() {
        return logLevel;
    }

    @Override
    public ApplicationType getType() {
        return ApplicationType.WebGL;
    }

    @Override
    public int getVersion() {
        return 0;
    }

    @Override
    public long getJavaHeap() {
        return 0;
    }

    @Override
    public long getNativeHeap() {
        return 0;
    }

    @Override
    public Preferences getPreferences (String name) {
        Preferences pref = prefs.get(name);
        if (pref == null) {
            pref = new TeaVMPreferences(name);
            prefs.put(name, pref);
        }
        return pref;
    }

    @Override
    public Clipboard getClipboard() {
        return clipboard;
    }

    @Override
    public void postRunnable(Runnable runnable) {
        runnables.add(runnable);
    }

    @Override
    public void exit() {
    }

    @Override
    public void addLifecycleListener(LifecycleListener listener) {
        lifecycleListeners.add(listener);
    }

    @Override
    public void removeLifecycleListener(LifecycleListener listener) {
        lifecycleListeners.removeValue(listener, true);
    }

}
