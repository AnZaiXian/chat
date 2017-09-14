package com.bawei.entity;

import lombok.Data;

/**
 * Created by mypc on 2017/8/9.
 */
//存储 用户-客服 聊天关系的数据模型
@Data
public class LinkData {
	private String id1;
	private String id2;


	public LinkData(String id1, String id2) {
		this.id1 = id1;
		this.id2 = id2;
	}

	public LinkData() {
	}
}
