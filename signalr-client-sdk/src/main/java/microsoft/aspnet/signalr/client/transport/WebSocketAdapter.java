package microsoft.aspnet.signalr.client.transport;

import microsoft.aspnet.signalr.client.UpdateableCancellableFuture;

/**
 * Created by Yitz on 3/7/2016.
 */
public abstract class WebSocketAdapter {

    protected UpdateableCancellableFuture<Void> _connectionFuture;
    protected DataResultCallback _callback;

    public abstract void connect();
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
}
