package com.bawei.action;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
@Controller
@RequestMapping("wrx")
public class TalkAction {
	/**
	 * 客户访问的页面
	 * @return
	 */
	@RequestMapping("/")
	public String index(){
		return "chat_user";
	}

	/**
	 * 客服回复的页面
	 * @return
	 */
	@RequestMapping("admin_chat")
	public String admin(){
		return "chat_admin";

	}
}
