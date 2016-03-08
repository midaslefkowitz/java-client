/*
Copyright (c) Microsoft Open Technologies, Inc.
All Rights Reserved
See License.txt in the project root for license information.
*/

package microsoft.aspnet.signalr.client.transport;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;

import microsoft.aspnet.signalr.client.ConnectionBase;
import microsoft.aspnet.signalr.client.Logger;
import microsoft.aspnet.signalr.client.SignalRFuture;
import microsoft.aspnet.signalr.client.UpdateableCancellableFuture;
import microsoft.aspnet.signalr.client.http.HttpConnection;

/**
 * Implements the WebsocketTransport for the Java SignalR library
 * Created by stas on 07/07/14.
 */
public class WebsocketTransport extends HttpClientTransport {

    WebSocketAdapter mWebSocketAdapter = null;
    private UpdateableCancellableFuture<Void> mConnectionFuture;

    public WebsocketTransport(Logger logger) {
        super(logger);
    }

    public WebsocketTransport(Logger logger, WebSocketAdapter webSocketAdapter) {
        super(logger);
        mWebSocketAdapter = webSocketAdapter;
    }

    public WebsocketTransport(Logger logger, HttpConnection httpConnection) {
        super(logger, httpConnection);
    }

    @Override
    public String getName() {
        return "webSockets";
    }

    @Override
    public boolean supportKeepAlive() {
        return true;
    }

    @Override
    public SignalRFuture<Void> start(ConnectionBase connection, ConnectionType connectionType, final DataResultCallback callback) {
        final String connectionString = connectionType == ConnectionType.InitialConnection ? "connect" : "reconnect";

        final String transport = getName();
        final String connectionToken = connection.getConnectionToken();
        final String messageId = connection.getMessageId() != null ? connection.getMessageId() : "";
        final String groupsToken = connection.getGroupsToken() != null ? connection.getGroupsToken() : "";
        final String connectionData = connection.getConnectionData() != null ? connection.getConnectionData() : "";


        String url = null;
        try {
            url = connection.getUrl() + "signalr/" + connectionString + '?'
                    + "connectionData=" + URLEncoder.encode(URLEncoder.encode(connectionData, "UTF-8"), "UTF-8")
                    + "&connectionToken=" + URLEncoder.encode(URLEncoder.encode(connectionToken, "UTF-8"), "UTF-8")
                    + "&groupsToken=" + URLEncoder.encode(groupsToken, "UTF-8")
                    + "&messageId=" + URLEncoder.encode(messageId, "UTF-8")
                    + "&transport=" + URLEncoder.encode(transport, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        mConnectionFuture = new UpdateableCancellableFuture<Void>(null);

        URI uri;
        try {
            uri = new URI(url);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            mConnectionFuture.triggerError(e);
            return mConnectionFuture;
        }

        if (mWebSocketAdapter == null) {
            mWebSocketAdapter = new TooTallSocketAdapter(uri, mLogger, getName(), mConnectionFuture, callback);
        }

        mWebSocketAdapter.connect();

        connection.closed(new Runnable() {
            @Override
            public void run() {
                mWebSocketAdapter.close();
            }
        });

        return mConnectionFuture;
    }

    @Override
    public SignalRFuture<Void> send(ConnectionBase connection, String data, DataResultCallback callback) {
        mWebSocketAdapter.send(data);
        return new UpdateableCancellableFuture<Void>(null);
    }
}