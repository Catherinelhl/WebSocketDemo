package bcaasc.io.websocketdemo.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import bcaasc.io.websocketdemo.R;
import bcaasc.io.websocketdemo.constants.Constants;
import bcaasc.io.websocketdemo.constants.URLConstants;
import bcaasc.io.websocketdemo.listener.WebSocketResponseListener;
import bcaasc.io.websocketdemo.service.WebSocketService;
import bcaasc.io.websocketdemo.tools.LogTool;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.jakewharton.rxbinding2.view.RxView;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import org.java_websocket.handshake.ServerHandshake;
import org.w3c.dom.Text;

import java.util.concurrent.TimeUnit;

/**
 * @author catherine.brainwilliam
 * @since 2018/11/30
 * <p>
 * 实现：
 * 1：点击按钮开始连接socket，然后连接成功
 * 2：开始发送心跳
 * 3：点击按钮发送想发送的信息
 * 4：设置一个输出文本输出服务器返回的数据信息
 */
public class MainActivity extends AppCompatActivity implements WebSocketResponseListener {
    @BindView(R.id.et_content)
    EditText etContent;
    @BindView(R.id.et_connect_url)
    EditText etConnectUrl;
    @BindView(R.id.tv_clean_log)
    TextView tvCleanLog;
    private String TAG = MainActivity.class.getSimpleName();

    @BindView(R.id.btn_connect_socket)
    Button btnConnectSocket;
    @BindView(R.id.btn_send_message)
    Button btnSendMessage;

    @BindView(R.id.tv_response_info)
    TextView tvResponseInfo;


    private WebSocketService webSocketService;
    //得到当前连接service的Intent
    private Intent webSocketServiceIntent;
    //用来判断当前是否是正在连接状态
    private boolean isTryConnect;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // to solve this problem:No Network Security Config specified, using platform default
//        StrictMode.ThreadPolicy policy = new
//                StrictMode.ThreadPolicy.Builder().permitAll().build();
//        StrictMode.setThreadPolicy(policy);

        ButterKnife.bind(this);
        initView();
        initListener();
    }

    private void initView() {
        etConnectUrl.setText(URLConstants.webSocketURL);
    }

    private void initListener() {
        RxView.clicks(btnConnectSocket).throttleFirst(1, TimeUnit.SECONDS)
                .subscribe(new Observer<Object>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Object o) {

                        String status = btnConnectSocket.getText().toString();
                        // 如果当前是未连接的状态
                        if (TextUtils.equals(status, "ConnectSocket")) {
                            //如果当前是正在连接
                            if (isTryConnect) {
                                Toast.makeText(MainActivity.this, "连接中....", Toast.LENGTH_SHORT);
                            } else {
                                // 连接WebSocketService
                                URLConstants.inputURL = etConnectUrl.getText().toString();
                                connectWebSocketService();
                                isTryConnect = true;
                            }
                        } else {
                            webSocketService.closeWebSocket();
                            //点击关闭当前连接，并且修改文本
                            setConnectStatus(true);

                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        setConnectStatus(true);

                    }

                    @Override
                    public void onComplete() {

                    }
                });
        RxView.clicks(btnSendMessage).throttleFirst(800, TimeUnit.MILLISECONDS)
                .subscribe(new Observer<Object>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Object o) {
                        String message = etContent.getText().toString();
                        if (TextUtils.isEmpty(message)) {
                            message = "{\"bean\":{\"path\":\"/getBalance\"},\"walletVO\":{\"walletAddress\":\"asdgfadhsgdjhklgfhdgsfadsd\"},\"remoteInfoVO\":{\"realIP\":\"35.194.115.233\"}}";
                        }
                        if (webSocketService != null) {
                            webSocketService.sendMessage(message);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
        RxView.clicks(tvCleanLog).throttleFirst(1, TimeUnit.SECONDS)
                .subscribe(new Observer<Object>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Object o) {
                        tvResponseInfo.setText("");
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    private void connectWebSocketService() {
        LogTool.d(TAG, "connectWebSocketService");
        if (webSocketService != null) {
            LogTool.d(TAG, "start");
            connectDirect();
        } else {
            LogTool.d(TAG, "bind");
            //绑定当前服务
            webSocketServiceIntent = new Intent(MainActivity.this, WebSocketService.class);
            bindService(webSocketServiceIntent, webSocketServiceConnect, Context.BIND_AUTO_CREATE);
        }
    }

    /**
     * Defines callbacks for service binding, passed to bindService()
     */
    private ServiceConnection webSocketServiceConnect = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            WebSocketService.WebSocketBinder binder = (WebSocketService.WebSocketBinder) service;
            webSocketService = binder.getService();
            connectDirect();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            //断开连接，如果是异常断开，应该让其重新连接上
            LogTool.d(TAG, "onServiceDisconnected");
            if (webSocketService != null) {
                webSocketService.onUnbind(webSocketServiceIntent);
            }
            connectWebSocketService();
        }
    };

    private void connectDirect() {
        webSocketService.connectWebSocket(this);

    }

    @Override
    public void onOpen(ServerHandshake handshake) {
        isTryConnect = false;
        setConnectStatus(false);
        if (webSocketService != null) {
            webSocketService.sendPing();
        }
        setText("Open Status:" + handshake.getHttpStatus() + "\nOpen Message:" + handshake.getHttpStatusMessage());

    }

    @Override
    public void onMessage(String message) {
        if (TextUtils.equals(message, Constants.PING)) {
            setText("-------- heartBeat ---------");
        } else {
            setText("收到发出内容:" + message);
        }

    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        LogTool.d(TAG, "Code:" + code + "\nReason:" + reason);
        if (code == 1000) {
            //close success
            setConnectStatus(true);
            setText("Close WebSocket Success.Code:" + code);
        } else {
            setText("Code:" + code + "\nReason:" + reason);

        }

    }

    @Override
    public void onError(Exception ex) {
        isTryConnect = false;
        setText("Error:" + ex.getMessage());
        webSocketService.closeWebSocket();
        setConnectStatus(false);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (webSocketService != null) {
            webSocketService.onUnbind(webSocketServiceIntent);
        }
    }


    private void setText(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String currentText = tvResponseInfo.getText().toString();
                tvResponseInfo.setText(TextUtils.isEmpty(currentText) ? message : currentText + "\n" + message);
            }
        });
    }

    /**
     * 设置接下来的状态
     *
     * @param isConnect 是否是显示连接
     */
    private void setConnectStatus(boolean isConnect) {
        btnConnectSocket.setText(isConnect ? "ConnectSocket" : "CloseWebSocket");
        btnConnectSocket.setTextColor(getResources().getColor(isConnect ? R.color.colorPrimaryDark : R.color.colorAccent));
    }
}
