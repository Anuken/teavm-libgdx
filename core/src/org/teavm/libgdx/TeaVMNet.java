package org.teavm.libgdx;

import com.badlogic.gdx.Net;
import com.badlogic.gdx.net.*;
import org.teavm.jso.ajax.XMLHttpRequest;
import org.teavm.jso.browser.Window;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class TeaVMNet implements Net {

    @Override
    public void sendHttpRequest(HttpRequest httpRequest, HttpResponseListener httpResponseListener) {
        XMLHttpRequest req = XMLHttpRequest.create();
        req.open(httpRequest.getMethod(), httpRequest.getUrl());
        for(Entry<String, String> e : httpRequest.getHeaders().entrySet()){
            req.setRequestHeader(e.getKey(), e.getValue());
        }
        req.setOnReadyStateChange(() -> {
            if (req.getReadyState() != XMLHttpRequest.DONE) {
                return;
            }

            int statusGroup = req.getStatus() / 100;
            if (statusGroup != 2 && statusGroup != 3) {
                httpResponseListener.failed(new IOException("HTTP status: " +
                req.getStatus() + " " + req.getStatusText()));
            } else {
                httpResponseListener.handleHttpResponse(new HttpResponse(){
                    @Override
                    public byte[] getResult(){
                        throw new UnsupportedOperationException("Not implemented");
                    }

                    @Override
                    public String getResultAsString(){
                        return req.getResponseText();
                    }

                    @Override
                    public InputStream getResultAsStream(){
                        throw new UnsupportedOperationException("Not implemented");
                    }

                    @Override
                    public HttpStatus getStatus(){
                        return new HttpStatus(req.getStatus());
                    }

                    @Override
                    public String getHeader(String name){
                        return req.getResponseHeader(name);
                    }

                    @Override
                    public Map<String, List<String>> getHeaders(){
                        throw new UnsupportedOperationException("Not implemented");
                    }
                });
            }
        });
        req.send();
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
