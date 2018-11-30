package bcaasc.io.websocketdemo.interval;

import bcaasc.io.websocketdemo.constants.Constants;
import bcaasc.io.websocketdemo.listener.SendPingListener;
import bcaasc.io.websocketdemo.tools.LogTool;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

import java.util.concurrent.TimeUnit;

/**
 * @author catherine.brainwilliam
 * @since 2018/11/30
 * 通知websocket发送ping
 */
public class IntervalEvent {
    private static String TAG = IntervalEvent.class.getSimpleName();
    private static Disposable sendPingDisposable;

    /**
     * 定义通过webSocket发送ping-心跳
     * 默认值是60s
     * onClose:1006
     * onClose:The connection was closed because the other endpoint did not respond with a pong in time. For more information check:
     * https://github.com/TooTallNate/Java-WebSocket/wiki/Lost-connection-detection
     */
    public static void sendPing(final SendPingListener sendPingListener) {
        Observable.interval(0, Constants.SEND_PING_TIME, TimeUnit.SECONDS)
                .subscribe(new Observer<Long>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        sendPingDisposable = d;
                    }


                    @Override
                    public void onNext(Long aLong) {
                        if (sendPingListener != null) {
                            sendPingListener.sendPing();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        LogTool.e(TAG, e.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        closeSendPing();
                    }
                });

    }

    /**
     * 关闭SendPing定时请求
     */
    private static void closeSendPing() {
        if (sendPingDisposable != null) {
            sendPingDisposable.dispose();
        }
    }

}
