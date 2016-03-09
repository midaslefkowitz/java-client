/*
Copyright (c) Microsoft Open Technologies, Inc.
All Rights Reserved
See License.txt in the project root for license information.
*/

package microsoft.aspnet.signalr.client.transport;

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

        mConnectionFuture = new UpdateableCancellableFuture<Void>(null);

        if (mWebSocketAdapter == null) {
            mWebSocketAdapter = new TooTallSocketAdapter(mLogger, getName());
        }

        mWebSocketAdapter.setCallback(callback);
        mWebSocketAdapter.setConnectionFuture(mConnectionFuture);

        String url = mWebSocketAdapter.createURL(connection, connectionType, getName());

        mWebSocketAdapter.connect(url);

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