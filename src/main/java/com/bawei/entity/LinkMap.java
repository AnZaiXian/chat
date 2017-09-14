package com.bawei.entity;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mypc on 2017/8/9.
 */
// 提供一个存储聊天关系的列表
@Data
public class LinkMap {
	// 用户 和 客服 关联的信息
	public static List<LinkData> LinkList = new ArrayList<LinkData>();
	//当前客服的列表  如果聊天 则把客服id 移出列表
	public static List<String> adminList = new ArrayList<String>();
}
