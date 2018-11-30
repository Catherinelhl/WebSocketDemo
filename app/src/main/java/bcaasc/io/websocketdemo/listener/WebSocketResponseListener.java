package bcaasc.io.websocketdemo.listener;

import org.java_websocket.handshake.ServerHandshake;

/**
 * @author catherine.brainwilliam
 * @since 2018/11/30
 */
public interface WebSocketResponseListener {
    void onOpen(ServerHandshake handshake);

    void onMessage(String message);

    void onClose(int code, String reason, boolean remote);

    void onError(Exception ex);
}
