# WebSocketDemo_S
this is a demo about how to use WebSocket by Android
#### WebSocket

Android采用：[Java-WebSocket](https://github.com/TooTallNate/Java-WebSocket)


可用测试server host：

1. ws://121.40.165.18:8800
 > 方式一会很容易close，且code：1006的状态。
2. ws://echo.websocket.org
> 方式二是目前采用的比较稳定的host，他会将client的send message 返回给我们以示收到。因为Android这边没有在WebSocketClient类里面找到作者定义的收到Server返回心跳的信息，所以可以自定义一个约定好的HeartBeat信息然后在onMessage里面进行判别即可。

项目：
此WebSocketDemo是将连接WebSockClient放在了Android's Service里面。这样和主界面的操作互不干扰。



