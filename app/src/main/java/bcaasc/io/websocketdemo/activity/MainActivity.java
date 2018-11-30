package bcaasc.io.websocketdemo.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.StrictMode;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import bcaasc.io.websocketdemo.R;
import bcaasc.io.websocketdemo.constants.Constants;
import bcaasc.io.websocketdemo.constants.URLConstants;
import bcaasc.io.websocketdemo.listener.WebSocketResponseListener;
import bcaasc.io.websocketdemo.service.WebSocketService;
import bcaasc.io.websocketdemo.tools.LogTool;
import butterknife.BindView;
import butterknife.ButterKnife;
import org.java_websocket.handshake.ServerHandshake;

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
    private String TAG = MainActivity.class.getSimpleName();

    @BindView(R.id.tv_connect_url)
    TextView tvConnectUrl;
    @BindView(R.id.btn_connect_socket)
    Button btnConnectSocket;
    @BindView(R.id.btn_send_message)
    Button btnSendMessage;

    @BindView(R.id.tv_response_info)
    TextView tvResponseInfo;


    private WebSocketService webSocketService;
    //得到当前连接service的Intent
    private Intent webSocketServiceIntent;

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
        tvConnectUrl.setText("URL:【" + URLConstants.webSocketURL + "】");
    }

    private void initListener() {
        btnConnectSocket.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 连接WebSocketService
                connectWebSocketService();

            }
        });
        btnSendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = etContent.getText().toString();
                if (TextUtils.isEmpty(message)) {
                    message = "{\"bean\":{\"path\":\"/getBalance\"},\"walletVO\":{\"walletAddress\":\"asdgfadhsgdjhklgfhdgsfadsd\"},\"remoteInfoVO\":{\"realIP\":\"35.194.115.233\"}}";
                }
                if (webSocketService != null) {
                    webSocketService.sendMessage(message);
                }
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
        if (webSocketService != null) {
            webSocketService.sendPing();
        }
        setText("Open Status:" + handshake.getHttpStatus() + "\nOpen Message:" + handshake.getHttpStatusMessage());

    }

    @Override
    public void onMessage(String message) {
        if (TextUtils.equals(message, Constants.PING)) {
            setText("收到 heartBeat");
        } else {
            setText("Message:" + message);
        }

    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        setText("Code:" + code + "\nReason:" + reason);

    }

    @Override
    public void onError(Exception ex) {
        setText("Error:" + ex.getMessage());

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
                tvResponseInfo.setText("Response Info:\n -----------\n" + message + "\n -----------");

            }
        });
    }
}
