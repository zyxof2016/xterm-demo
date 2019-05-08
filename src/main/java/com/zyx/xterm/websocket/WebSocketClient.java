package com.zyx.xterm.websocket;

import com.zyx.xterm.websocket.handler.local.LocalTermClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.TextMessage;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
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
        sendMessage("szzx@window7$");
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
            String[] commands = partitionCommandLine(commandStr);
            System.out.println(org.apache.tomcat.util.buf.StringUtils.join(commands));
            ProcessBuilder pb = new ProcessBuilder(commands);
            pb.redirectErrorStream(true);
            Process p = pb.start();
            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream(), "gbk"));
            String line;
            while ((line = br.readLine()) != null){
                System.out.println(new String(line.getBytes(), "UTF-8"));
                sendMessage(new String(line.getBytes(), "UTF-8"));
                sendMessage("\r\n");
            }
            sendMessage("szzx@window7$");
            p.waitFor();
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

    /**
     * Splits the command into a unix like command line structure. Quotes and
     * single quotes are treated as nested strings.
     *
     * @param command
     * @return
     */
    public static String[] partitionCommandLine(String command) {

        ArrayList<String> commands = new ArrayList<String>();

        String os=System.getProperties().getProperty("os.name");
        if(os!=null && (os.startsWith("win") || os.startsWith("Win"))){
            commands.add("CMD.EXE");
            commands.add("/C");
            commands.add(command);
        }else{
            int index = 0;
            StringBuffer buffer = new StringBuffer(command.length());
            boolean isApos = false;
            boolean isQuote = false;
            while(index < command.length()) {
                char c = command.charAt(index);
                switch(c) {
                    case ' ':
                        if(!isQuote && !isApos) {
                            String arg = buffer.toString();
                            buffer = new StringBuffer(command.length() - index);
                            if(arg.length() > 0) {
                                commands.add(arg);
                            }
                        } else {
                            buffer.append(c);
                        }
                        break;
                    case '\'':
                        if(!isQuote) {
                            isApos = !isApos;
                        } else {
                            buffer.append(c);
                        }
                        break;
                    case '"':
                        if(!isApos) {
                            isQuote = !isQuote;
                        } else {
                            buffer.append(c);
                        }
                        break;
                    default:
                        buffer.append(c);
                }
                index++;
            }
            if(buffer.length() > 0) {
                String arg = buffer.toString();
                commands.add(arg);
            }
        }
        return commands.toArray(new String[commands.size()]);
    }
}
