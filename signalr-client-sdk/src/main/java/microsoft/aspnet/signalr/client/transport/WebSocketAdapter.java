package microsoft.aspnet.signalr.client.transport;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import microsoft.aspnet.signalr.client.Connection;
import microsoft.aspnet.signalr.client.ConnectionBase;
import microsoft.aspnet.signalr.client.UpdateableCancellableFuture;

/**
 * Created by Yitz on 3/7/2016.
 */
public abstract class WebSocketAdapter {

    protected UpdateableCancellableFuture<Void> _connectionFuture;
    protected DataResultCallback _callback;

    public abstract void connect(String url);
    public abstract void close();
    public abstract void send(String data);
    public abstract void onMessage(String s);
    public abstract void onOpen();

    public void setCallback(DataResultCallback callback) {
        _callback = callback;
    }

    public void setConnectionFuture(UpdateableCancellableFuture<Void> connectionFuture) {
        _connectionFuture = connectionFuture;
    }

    public String createURL(ConnectionBase connection, ConnectionType connectionType, String name) {
        final String connectionString = connectionType == ConnectionType.InitialConnection ? "connect" : "reconnect";

        final String transport = name;
        final String protocolVersion = Connection.PROTOCOL_VERSION.toString();
        final String connectionToken = connection.getConnectionToken();
        final String messageId = connection.getMessageId() != null ? connection.getMessageId() : "";
        final String groupsToken = connection.getGroupsToken() != null ? connection.getGroupsToken() : "";
        final String connectionData = connection.getConnectionData() != null ? connection.getConnectionData() : "";

        String url = null;
        try {
            url = connection.getUrl() + "signalr/" + connectionString + '?'
                    + "transport=" + URLEncoder.encode(transport, "UTF-8")
                    + "&clientProtocol=" + protocolVersion
                    + "&connectionData=" + URLEncoder.encode(connectionData, "UTF-8")
                    + "&connectionToken=" + URLEncoder.encode(connectionToken, "UTF-8")
                    + "&groupsToken=" + URLEncoder.encode(groupsToken, "UTF-8")
                    + "&messageId=" + URLEncoder.encode(messageId, "UTF-8");
            if (connection.getQueryString() != null) {
                url += "&" + connection.getQueryString();
            }
            if(url.startsWith("https://")){
                url = url.replace("https://", "wss://");
            } else if(url.startsWith("http://")){
                url = url.replace("http://", "ws://");
            }

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return url;
    }
}
