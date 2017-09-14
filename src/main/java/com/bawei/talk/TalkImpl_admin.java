package com.bawei.talk;

import com.bawei.entity.LinkData;
import com.bawei.entity.LinkMap;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Created by mypc on 2017/8/8.
 *
 * 就是getAsyncRemote是非阻塞式的，getBasicRemote是阻塞式的，表示不懂。推送消息的过程中遇到了一个bug，
 * CSDN的一位网友正好遇到过这个bug, 于是顺便把getAsyncRemote()和getBasicRemote() 的区别给请教了一下，那位网友是这样解释的：
   getAsyncRemote()和getBasicRemote()确实是异步与同步的区别，大部分情况下，推荐使用getAsyncRemote()。由于getBasicRemote()的
   同步特性，并且它支持部分消息的发送即sendText(xxx,boolean isLast). isLast的值表示是否一次发送消息中的部分消息，对于如下情况:
   session.getBasicRemote().sendText(message, false);
   session.getBasicRemote().sendBinary(data);
   session.getBasicRemote().sendText(message, true);
   由于同步特性，第二行的消息必须等待第一行的发送完成才能进行，而第一行的剩余部分消息要等第二行发送完才能继续发送，所以在第二行
   会抛出IllegalStateException异常。如果要使用getBasicRemote()同步发送消息，则避免尽量一次发送全部消息，使用部分消息来发送。
 */
@ServerEndpoint(value = "/work")
@Component
public class TalkImpl_admin {
	//静态变量，用来记录当前在线连接数。应该把它设计成线程安全的。
	private static int onlineCount = 0;

	//concurrent包的线程安全Set，用来存放每个客户端对应的MyWebSocket对象。
	public static CopyOnWriteArraySet<TalkImpl_admin> webSocketSet = new CopyOnWriteArraySet<TalkImpl_admin>();

	//与某个客户端的连接会话，需要通过它来给客户端发送数据
	private Session session;

	/**
	 * 连接建立成功调用的方法
	 *
	 **/
	@OnOpen
	public void onOpen(Session session) {
		this.session = session;
		webSocketSet.add(this);     //加入set中
		addOnlineCount();           //在线数加1
		System.out.println("有新客服加入！当前在线人数为" + getOnlineCount()+"/n id 是");
		//把客服加入在线客服的集合
		LinkMap.adminList.add(session.getId());
		try{
		this.session.getBasicRemote().sendText("请准备开始工作，并打开一个信息聊天界面");
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * 连接关闭调用的方法
	 */
	@OnClose
	public void onClose() {
		webSocketSet.remove(this);  //从set中删除
		//把当前窗口的客服 从在线客服列表中删除
		LinkMap.adminList.remove(this);
		subOnlineCount();           //在线数减1
		System.out.println("有一客服连接关闭！当前客服在线数为" + getOnlineCount());
	}

	/**
	 * 收到客服消息后调用的方法
	 *
	 * @param message 客服发送过来的消息*/
	@OnMessage
	public void onMessage(String message, Session session) {
		System.out.println("来自客服的消息:" + message);
		String id ="";
		//找到 用户对应的 用户id
		for(LinkData data:LinkMap.LinkList){
			if (session.getId().equals(data.getId2())){
				id=data.getId1();
			}
		}
		//开始转发（遍历客户的session 的集合）
		int yes=0;// 客户是否在线
		for (TalkImpl_user item : TalkImpl_user.webSocketSet) {
			try {
				if(item.getSession().getId().equals(id)){
					item.sendMessage(message);
					yes =1;
					break;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if(yes == 0){
			try {
				//websocket  session发送文本消息有两个方法  getAsyncRemote()
			//session.getBasicRemote().sendText("咨询用户已关闭连接，请关闭该窗口");
				session.getAsyncRemote().sendText("咨询用户已关闭连接，请关闭该窗口");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 发生错误时调用**/
	 @OnError
	 public void onError(Session session, Throwable error) {
	 System.out.println("发生错误");
	 error.printStackTrace();
	 }


	 public void sendMessage(String message) throws IOException {
	 this.session.getBasicRemote().sendText(message);
	 //this.session.getAsyncRemote().sendText(message);
	 }


	 /**
	  * 群发自定义消息
	  * */
	public static void sendInfo(String message) throws IOException {
		for (TalkImpl_admin item : webSocketSet) {
			try {
				item.sendMessage(message);
			} catch (IOException e) {
				continue;
			}
		}
	}

	public static synchronized int getOnlineCount() {
		return onlineCount;
	}

	public static synchronized void addOnlineCount() {
		TalkImpl_admin.onlineCount++;
	}

	public static synchronized void subOnlineCount() {
		TalkImpl_admin.onlineCount--;
	}

	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}
}
