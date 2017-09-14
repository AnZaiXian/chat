package com.bawei.talk;

import com.bawei.entity.LinkData;
import com.bawei.entity.LinkMap;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Created by mypc on 2017/8/8.
 */
@ServerEndpoint(value = "/chat")
@Component
public class TalkImpl_user {
	//静态变量，用来记录当前在线连接数。应该把它设计成线程安全的。
	private static int onlineCount = 0;

	//concurrent包的线程安全Set，用来存放每个客户端对应的MyWebSocket对象。
	public static CopyOnWriteArraySet<TalkImpl_user> webSocketSet = new CopyOnWriteArraySet<TalkImpl_user>();

	//与某个客户端的连接会话，需要通过它来给客户端发送数据
	private Session session;

	/**
	 * 连接建立成功调用的方法*/
	@OnOpen
	public void onOpen(Session session) {
		this.session = session;
		webSocketSet.add(this);     //加入set中
		addOnlineCount();           //在线数加1
		System.out.println("有新客户加入！当前在线人数为" + getOnlineCount());
		Random r = new Random();
		//随机指定一个客服(获取Id)
		if(LinkMap.adminList.size()<1){
			try{
				this.session.getBasicRemote().sendText("抱歉，客服在为其他人服务，请稍后刷新");
			} catch (IOException e) {
				e.printStackTrace();
			}
			return;
		}
		int i = r.nextInt(LinkMap.adminList.size());
		String id = LinkMap.adminList.get(i);
		//绑定对话关系(用户 session id，客服 session id)
		LinkMap.LinkList.add(new LinkData(session.getId(),id));
		//把绑定的客服移出在线列表
		LinkMap.adminList.remove(id);
		try{
		this.session.getBasicRemote().sendText("欢迎你的咨询,在线客服人员将为你服务");
		this.session.getBasicRemote().sendText("请问您有什么问题需要咨询？");
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
		subOnlineCount();           //在线数减1
		System.out.println("有一客户连接关闭！当前客户在线人数为" + getOnlineCount());
	}

	/**
	 * 收到客户端消息后调用的方法
	 *
	 * @param message 客户端发送过来的消息 转发给客服*/
	@OnMessage
	public void onMessage(String message, Session session) {
		System.out.println("来自客户的消息:" + message);
		String id ="";
		//找到 用户对应的 客服id
		for(LinkData data:LinkMap.LinkList){
			if (session.getId().equals(data.getId1())){
				id=data.getId2();
			}
		}
		//开始转发（遍历客服的session 的集合）
		int yes = 0;
		for (TalkImpl_admin item : TalkImpl_admin.webSocketSet) {
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
				session.getBasicRemote().sendText("抱歉，客服意外下线了，刷新页面为你重新分配客服");
			} catch (IOException e) {
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
		for (TalkImpl_user item : webSocketSet) {
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
		TalkImpl_user.onlineCount++;
	}

	public static synchronized void subOnlineCount() {
		TalkImpl_user.onlineCount--;
	}

	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}
}
