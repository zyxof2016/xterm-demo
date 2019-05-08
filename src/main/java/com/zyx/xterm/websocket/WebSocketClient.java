package com.zyx.xterm.websocket;

import com.zyx.xterm.websocket.handler.local.LocalTermClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArraySet;

@ServerEndpoint("/webshell/local")
@Component
public class WebSocketClient {
    // 用来记录当前连接数的变量
    private static volatile int onlineCount = 0;

    // concurrent包的线程安全Set，用来存放每个客户端对应的MyWebSocket对象
    private static CopyOnWriteArraySet<WebSocketClient> webSocketSet = new CopyOnWriteArraySet<WebSocketClient>();

    // 与某个客户端的连接会话，需要通过它来与客户端进行数据收发
    private Session session;

    private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketClient.class);

    @OnOpen
    public void onOpen(Session session) throws Exception {
        this.session = session;
        webSocketSet.add(this);
        Properties prop = System.getProperties();
        sendMessage("欢迎" + prop.get("user.name") + "登录" + prop.get("os.name"));
        sendMessage("\r\n");
        LOGGER.info("Open a websocket.");
    }

    @OnClose
    public void onClose() {
        webSocketSet.remove(this);
        LOGGER.info("Close a websocket. ");
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        try {
            exeCmd(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @OnError
    public void onError(Session session, Throwable error) {
        LOGGER.error("Error while websocket. ", error);
    }

    public void sendMessage(String message) throws Exception {
        if (this.session.isOpen()) {
            this.session.getBasicRemote().sendText(message);
        }
    }

    public void exeCmd(String commandStr) {
        try {
            String[] commands = {commandStr};
            ProcessBuilder pb = new ProcessBuilder(commands);
            pb.redirectErrorStream(true);
            Process p = pb.start();
            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while ((line = br.readLine()) != null){
                sendMessage(line);
            }
            p.waitFor();
            sendMessage("\r\n");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static synchronized int getOnlineCount() {
        return onlineCount;
    }

    public static synchronized void addOnlineCount() {
        WebSocketClient.onlineCount++;
    }

    public static synchronized void subOnlineCount() {
        WebSocketClient.onlineCount--;
    }
}
