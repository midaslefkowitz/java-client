package microsoft.aspnet.signalr.client.transport;

import com.google.gson.Gson;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.exceptions.InvalidDataException;
import org.java_websocket.framing.Framedata;
import org.java_websocket.handshake.ServerHandshake;
import org.java_websocket.util.Charsetfunctions;

import java.net.URI;
import java.net.URISyntaxException;

import microsoft.aspnet.signalr.client.LogLevel;
import microsoft.aspnet.signalr.client.Logger;
import microsoft.aspnet.signalr.client.UpdateableCancellableFuture;

/**
 * Created by Yitz on 3/7/2016.
 */
public class TooTallSocketAdapter extends WebSocketAdapter {

    private static final Gson gson = new Gson();
    private WebSocketClient _tooTallNateSocket;
    private Logger _logger;
    private String _name;
    private String _prefix;

    public TooTallSocketAdapter(Logger logger, String name) {
        _logger = logger;
        _name = name;
    }

    public void onOpen() {
        _connectionFuture.setResult(null);
    }

    public void onMessage(String message) {
        _callback.onData(message);
    }

    public void connect(String url) {

        URI uri;
        try {
            uri = new URI(url);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            _connectionFuture.triggerError(e);
            return;
        }

        if (_tooTallNateSocket == null) {
            _tooTallNateSocket = new WebSocketClient(uri) {
                @Override
                public void onOpen(ServerHandshake serverHandshake) {
                    TooTallSocketAdapter.this.onOpen();
                }

                @Override
                public void onMessage(String s) {
                    TooTallSocketAdapter.this.onMessage(s);
                }

                @Override
                public void onClose(int i, String s, boolean b) {
                    _tooTallNateSocket.close();
                }

                @Override
                public void onError(Exception e) {
                    _tooTallNateSocket.close();
                }

                @Override
                public void onFragment(Framedata frame) {
                    try {
                        String decodedString = Charsetfunctions.stringUtf8(frame.getPayloadData());

                        if (decodedString.equals("]}")) {
                            return;
                        }

                        if (decodedString.endsWith(":[") || null == _prefix) {
                            _prefix = decodedString;
                            return;
                        }

                        String simpleConcatenate = _prefix + decodedString;

                        if (isJSONValid(simpleConcatenate)) {
                            onMessage(simpleConcatenate);
                        } else {
                            String extendedConcatenate = simpleConcatenate + "]}";
                            if (isJSONValid(extendedConcatenate)) {
                                onMessage(extendedConcatenate);
                            } else {
                                log("invalid json received:" + decodedString, LogLevel.Critical);
                            }
                        }
                    } catch (InvalidDataException e) {
                        e.printStackTrace();
                    }
                }
            };
        }
        _tooTallNateSocket.connect();
    }

    public void close() {
        _tooTallNateSocket.close();
    }

    public void send(String data) {
        _tooTallNateSocket.send(data);
    }

    private String getName() {
        return _name;
    }

    private boolean isJSONValid(String test){
        try {
            gson.fromJson(test, Object.class);
            return true;
        } catch(com.google.gson.JsonSyntaxException ex) {
            return false;
        }
    }

    private void log(String message, LogLevel level) {
        _logger.log(getName() + " - " + message, level);
    }

    private void log(Throwable error) {
        _logger.log(getName() + " - Error: " + error.toString(), LogLevel.Critical);
    }
}
