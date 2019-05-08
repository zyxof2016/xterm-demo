package com.zyx.xterm.websocket.handler.local;

import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import javax.websocket.server.ServerEndpoint;
import java.util.Arrays;
import java.util.Date;
import java.util.Properties;

/**
 * 创建日期:2018年1月11日<br/>
 * 创建时间:下午10:09:11<br/>
 * 创建者    :yellowcong<br/>
 * 机能概要:
 */

@ServerEndpoint("/webshh/local")
@Component
public class LocalShellHandler extends TextWebSocketHandler {
	private LocalTermClient client ;
	
	//关闭连接后的处理
	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
		// TODO Auto-generated method stub
		super.afterConnectionClosed(session, status);
		//连接关闭
	}
	
	//建立socket连接
	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		// TODO Auto-generated method stub
		super.afterConnectionEstablished(session);

		//
		
		String info = "欢迎登录本地系统" + new Date().toLocaleString()+"\n";
		
		//写数据到服务器
		session.sendMessage(new TextMessage(info));
		
		Properties prop = System.getProperties();
		
		//操作系统名称
		String osName = "登录系统:\t"+prop.get("os.name").toString()+"\n";
		
		//用户名称
		String usrName = "登录用户:\t"+prop.get("user.name").toString()+"\n";
		
		//写数据到服务器
		session.sendMessage(new TextMessage(osName));
		session.sendMessage(new TextMessage(usrName));
				
				
		//建立socket连接
		client = new LocalTermClient(session);
	}

	//处理socker发送过来的消息
	@Override
	public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
		super.handleMessage(session, message);
		
		TextMessage msg = (TextMessage) message;
			//receive a close cmd ?
		if (Arrays.equals("exit".getBytes(), msg.asBytes())) {
			session.close();
			return ;
		}
		//判断客户端是否存在
		if(client != null) {
			//写入前台传递过来的命令，发送到目标服务器上
			String cmdStr = new String(msg.asBytes(), "UTF-8");
			
			//执行数据
			client.exeCmd(cmdStr);
		}
	}
	
}
