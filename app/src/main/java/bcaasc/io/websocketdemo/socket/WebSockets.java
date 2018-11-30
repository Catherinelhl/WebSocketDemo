package bcaasc.io.websocketdemo.socket;

import bcaasc.io.websocketdemo.constants.URLConstants;
import bcaasc.io.websocketdemo.interval.IntervalEvent;
import bcaasc.io.websocketdemo.listener.SendPingListener;
import bcaasc.io.websocketdemo.listener.WebSocketResponseListener;
import bcaasc.io.websocketdemo.tools.LogTool;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.nio.ByteBuffer;

/**
 * @author catherine.brainwilliam
 * @since 2018/11/30
 * 实现一个本地的webSocket
 */
public class WebSockets extends WebSocketClient {

    private String TAG = WebSockets.class.getSimpleName();

    public WebSockets(URI serverUri) {
        super(serverUri);
    }

    private WebSocketResponseListener webSocketResponseListener;

    public void setWebSocketListener(WebSocketResponseListener webSocketResponseListener) {
        this.webSocketResponseListener = webSocketResponseListener;
    }

    public static void main(String[] args) {
        URI uri = URI.create(URLConstants.webSocketURL);
        final WebSockets webSockets = new WebSockets(uri);
        // 更改发送sendPing的时间间隔，默认是60s，我们这里是30s
//        webSockets.setConnectionLostTimeout(30);

        Thread thread = new Thread(webSockets);
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            webSockets.close();
        }
        IntervalEvent.sendPing(new SendPingListener() {
            @Override
            public void sendPing() {
                System.out.println("sendPing");
                webSockets.sendPing();
            }
        });
    }

    @Override
    public void onOpen(ServerHandshake handshake) {
        LogTool.d(TAG, "onOpen:" + handshake.getHttpStatus());
//        System.out.println("onOpen:" + handshake.getHttpStatus());
        LogTool.d(TAG, "onOpen:" + handshake.getHttpStatusMessage());
//        System.out.println("onOpen:" + handshake.getHttpStatusMessage());
        if (webSocketResponseListener != null) {
            webSocketResponseListener.onOpen(handshake);
        }

    }

    @Override
    public void onMessage(String message) {
        LogTool.d(TAG, "onMessage:" + message);
//        System.out.println("onMessage:" + message);
        if (webSocketResponseListener != null) {
            webSocketResponseListener.onMessage(message);
        }

    }

    @Override
    public void onMessage(ByteBuffer bytes) {
        LogTool.d(TAG, bytes);
        super.onMessage(bytes);
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
//        System.out.println("onClose:" + code);
        LogTool.d(TAG, "onClose code:" + code);
        LogTool.d(TAG, "onClose reason:" + reason);
        LogTool.d(TAG, "onClose remote:" + remote);
//        System.out.println("onClose:" + reason);
        if (webSocketResponseListener != null) {
            webSocketResponseListener.onClose(code, reason, remote);
        }

    }

    @Override
    public void onError(Exception ex) {
//        System.out.println("onError:" + ex.getMessage());
        LogTool.e(TAG, "onError:" + ex.getMessage());
        ex.printStackTrace();
        if (webSocketResponseListener != null) {
            webSocketResponseListener.onError(ex);
        }
    }

}
