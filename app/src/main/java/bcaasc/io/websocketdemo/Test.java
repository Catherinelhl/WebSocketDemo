package bcaasc.io.websocketdemo;

import bcaasc.io.websocketdemo.socket.WebSockets;

import java.net.URI;

/**
 * @author catherine.brainwilliam
 * @since 2018/11/30
 */
public class Test {

    public static void main(String[] args) {
        WebSockets webSockets;
        URI uri = URI.create("ws://121.40.165.18:8800");
        webSockets = new WebSockets(uri);
        Thread thread = new Thread(webSockets);
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            webSockets.close();
        }
    }
}
