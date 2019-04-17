package bcaasc.io.websocketdemo.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import bcaasc.io.websocketdemo.constants.Constants;
import bcaasc.io.websocketdemo.constants.URLConstants;
import bcaasc.io.websocketdemo.interval.IntervalEvent;
import bcaasc.io.websocketdemo.listener.SendPingListener;
import bcaasc.io.websocketdemo.listener.WebSocketResponseListener;
import bcaasc.io.websocketdemo.socket.WebSockets;
import bcaasc.io.websocketdemo.tools.LogTool;

import java.net.URI;
import java.net.URL;

/**
 * @author catherine.brainwilliam
 * @since 2018/11/30
 */
public class WebSocketService extends Service {

    private String TAG = WebSocketService.class.getSimpleName();
    private final IBinder webSocketBinder = new WebSocketBinder();
    private WebSockets webSockets;
    private WebSocketResponseListener webSocketResponseListener;

    public void connectWebSocket(WebSocketResponseListener webSocketResponseListener) {
        this.webSocketResponseListener = webSocketResponseListener;
        String url = URLConstants.inputURL;
        if (TextUtils.isEmpty(url)) {
            url = URLConstants.webSocketURL;
        }
        webSockets = new WebSockets(URI.create(url));
        // set the interval time between of client and server
//        webSockets.setConnectionLostTimeout(30);
        webSockets.setWebSocketListener(webSocketResponseListener);
        Thread thread = new Thread(webSockets);
        thread.start();
//        try {
//            thread.join();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        } finally {
//            webSockets.close();
//        }

    }

    /**
     * 开始发送心跳
     */
    public void sendPing() {
        IntervalEvent.sendPing(new SendPingListener() {
            @Override
            public void sendPing() {
                if (webSockets != null && webSockets.isOpen()) {
                    LogTool.d(TAG, "sendPing");
                    //method 1st
//                    webSockets.sendPing();
                    //method 2nd
                    webSockets.send(Constants.PING);
                }
            }
        });
    }

    /**
     * 开始发送心跳
     */
    public void sendMessage(String info) {
        if (webSockets != null && webSockets.isOpen()) {
            webSockets.send(info);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return webSocketBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        LogTool.d(TAG, "onUnbind");
        if (webSockets != null) {
            webSockets.close();
        }
        return super.onUnbind(intent);
    }


    public void closeWebSocket() {
        if (webSockets != null) {
            webSockets.close();
            webSockets=null;
        }
    }

    public class WebSocketBinder extends Binder {
        public WebSocketService getService() {
            return WebSocketService.this;
        }
    }
}
