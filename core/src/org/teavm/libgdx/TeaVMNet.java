package org.teavm.libgdx;

import com.badlogic.gdx.Net;
import com.badlogic.gdx.net.ServerSocket;
import com.badlogic.gdx.net.ServerSocketHints;
import com.badlogic.gdx.net.Socket;
import com.badlogic.gdx.net.SocketHints;
import org.teavm.jso.browser.Window;

public class TeaVMNet implements Net {

    @Override
    public void sendHttpRequest(HttpRequest httpRequest, HttpResponseListener httpResponseListener) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void cancelHttpRequest(HttpRequest httpRequest) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public ServerSocket newServerSocket(Protocol protocol, String hostname, int port, ServerSocketHints hints) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public ServerSocket newServerSocket(Protocol protocol, int port, ServerSocketHints hints) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Socket newClientSocket(Protocol protocol, String host, int port, SocketHints hints) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public boolean openURI(String URI) {
        Window.current().open(URI, "_blank");
        return true;
    }
}
