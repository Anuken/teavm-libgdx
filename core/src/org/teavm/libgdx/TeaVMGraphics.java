/*
 *  Copyright 2015 Alexey Andreev.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.teavm.libgdx;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.Cursor.SystemCursor;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.glutils.GLVersion;
import org.teavm.jso.browser.Screen;
import org.teavm.jso.browser.Window;
import org.teavm.jso.core.JSArrayReader;
import org.teavm.jso.core.JSString;
import org.teavm.jso.dom.html.HTMLCanvasElement;
import org.teavm.jso.webgl.WebGLContextAttributes;
import org.teavm.jso.webgl.WebGLRenderingContext;

/**
 *
 * @author Alexey Andreev
 */
public class TeaVMGraphics implements Graphics {
    private HTMLCanvasElement element;
    private TeaVMApplicationConfig config;
    private WebGLRenderingContext context;
    long frameId = -1;
    float deltaTime;
    long lastTimeStamp;
    long time;
    int frames;
    float fps;
    private GL20 gl20;
    private GLVersion glVersion;

    public TeaVMGraphics(HTMLCanvasElement element, TeaVMApplicationConfig config) {
        this.element = element;
        this.config = config;

        WebGLContextAttributes attr = WebGLContextAttributes.create();
        attr.setAlpha(config.isAlphaEnabled());
        attr.setAntialias(config.isAntialiasEnabled());
        attr.setStencil(config.isStencilEnabled());
        attr.setPremultipliedAlpha(config.isPremultipliedAlpha());
        attr.setPreserveDrawingBuffer(config.isDrawingBufferPreserved());

        context = (WebGLRenderingContext)element.getContext("webgl");
        context.viewport(0, 0, element.getWidth(), element.getHeight());
        gl20 = new TeaVMGL20(context);

        String versionString = gl20.glGetString(GL20.GL_VERSION);
        String vendorString = gl20.glGetString(GL20.GL_VENDOR);
        String rendererString = gl20.glGetString(GL20.GL_RENDERER);
        glVersion = new GLVersion(Application.ApplicationType.WebGL, versionString, vendorString, rendererString);
    }

    @Override
    public void setGL20(GL20 gl20) {
        this.gl20 = gl20;
        Gdx.gl = gl20;
        Gdx.gl20 = gl20;
    }

    @Override
    public void setGL30(GL30 gl30) {
    }

    @Override
    public int getBackBufferWidth() {
        return getWidth();
    }

    @Override
    public int getBackBufferHeight() {
        return getHeight();
    }

    @Override
    public GLVersion getGLVersion() {
        return glVersion;
    }

    @Override
    public Monitor getPrimaryMonitor() {
        return getMonitor();
    }

    @Override
    public Monitor getMonitor() {
        return new TeaVMMonitor(0, 0, "Primary Monitor");
    }

    @Override
    public Monitor[] getMonitors() {
        return new Monitor[]{getPrimaryMonitor()};
    }

    @Override
    public DisplayMode[] getDisplayModes(Monitor monitor) {
        return getDisplayModes();
    }

    @Override
    public DisplayMode getDisplayMode() {
        return getDisplayModes()[0];
    }

    @Override
    public DisplayMode getDisplayMode(Monitor monitor) {
        return getDisplayMode();
    }

    @Override
    public boolean setFullscreenMode(DisplayMode displayMode) {
        return false;
    }

    @Override
    public boolean setWindowedMode(int width, int height) {
        return false;
    }

    @Override
    public void setUndecorated(boolean undecorated) {

    }

    @Override
    public void setResizable(boolean resizable) {

    }

    @Override
    public Cursor newCursor(Pixmap pixmap, int xHotspot, int yHotspot) {
        return () -> {};
    }

    @Override
    public void setCursor(Cursor cursor) {
        //TODO not implemented
    }

    @Override
    public void setSystemCursor(SystemCursor systemCursor) {
        //TODO not implemented
    }

    @Override
    public boolean isGL30Available() {
        return false;
    }

    @Override
    public GL20 getGL20() {
        return gl20;
    }

    @Override
    public GL30 getGL30() {
        return null;
    }

    @Override
    public int getWidth() {
        return element.getWidth();
    }

    @Override
    public int getHeight() {
        return element.getHeight();
    }

    @Override
    public long getFrameId() {
        return frameId;
    }

    @Override
    public float getDeltaTime() {
        return deltaTime;
    }

    @Override
    public float getRawDeltaTime() {
        return deltaTime;
    }

    @Override
    public int getFramesPerSecond() {
        return (int)fps;
    }

    @Override
    public GraphicsType getType() {
        return GraphicsType.WebGL;
    }

    @Override
    public float getPpiX() {
        return 96;
    }

    @Override
    public float getPpiY() {
        return 96;
    }

    @Override
    public float getPpcX() {
        return 96 / 2.54f;
    }

    @Override
    public float getPpcY() {
        return 96 / 2.54f;
    }

    @Override
    public float getDensity() {
        return 0;
    }

    @Override
    public boolean supportsDisplayModeChange() {
        return true;
    }

    @Override
    public DisplayMode[] getDisplayModes() {
        Window window = Window.current();
        Screen screen = window.getScreen();
        return new DisplayMode[] { new DisplayMode(screen.getWidth(), screen.getHeight(), 60, 8) {}};
    }

    @Override
    public void setTitle(String title) {
    }

    @Override
    public void setVSync(boolean vsync) {
    }

    @Override
    public BufferFormat getBufferFormat() {
        return new BufferFormat(8, 8, 8, 0, 16, config.isStencilEnabled() ? 8 : 0, 0, false);
    }

    @Override
    public boolean supportsExtension(String extension) {
        JSArrayReader<JSString> array = context.getSupportedExtensions();
        for (int i = 0; i < array.getLength(); ++i) {
            if (array.get(i).stringValue().equals(extension)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void setContinuousRendering(boolean isContinuous) {
    }

    @Override
    public boolean isContinuousRendering() {
        return false;
    }

    @Override
    public void requestRendering() {
    }

    @Override
    public boolean isFullscreen() {
        return false;
    }

    public void update () {
        long currTimeStamp = System.currentTimeMillis();
        deltaTime = (currTimeStamp - lastTimeStamp) / 1000.0f;
        lastTimeStamp = currTimeStamp;
        time += deltaTime;
        frames++;
        if (time > 1) {
            this.fps = frames;
            time = 0;
            frames = 0;
        }
    }

    static class TeaVMMonitor extends Monitor {
        protected TeaVMMonitor (int virtualX, int virtualY, String name) {
            super(virtualX, virtualY, name);
        }
    }
}
