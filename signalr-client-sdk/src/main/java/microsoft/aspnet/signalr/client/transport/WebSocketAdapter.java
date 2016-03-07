package microsoft.aspnet.signalr.client.transport;

import com.google.gson.Gson;

import java.net.URI;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.exceptions.InvalidDataException;
import org.java_websocket.framing.Framedata;
import org.java_websocket.handshake.ServerHandshake;
import org.java_websocket.util.Charsetfunctions;

import microsoft.aspnet.signalr.client.LogLevel;
import microsoft.aspnet.signalr.client.Logger;
import microsoft.aspnet.signalr.client.UpdateableCancellableFuture;

/**
 * Created by Yitz on 3/7/2016.
 */
public abstract class WebSocketAdapter {
    public abstract void connect();
    public abstract void close();
    public abstract void send(String data);
}
