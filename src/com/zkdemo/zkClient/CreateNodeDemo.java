package com.zkdemo.zkClient;

import org.I0Itec.zkclient.ZkClient;

/**
 * 运用zkClient api
 */
public class CreateNodeDemo {
	public static void main(String[] args) {
		ZkClient client = new ZkClient("192.168.205.101:2181", 5000);
		String path = "/zk-client/c1";
		// 递归创建节点
		client.createPersistent(path, true);
	}
}
