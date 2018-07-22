package com.zkdemo.election;

import java.io.IOException;

import javax.servlet.ServletException;

import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.ValveBase;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.curator.framework.recipes.cache.TreeCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;

/**
 * Zookeeper Master选举
 * 用Tomcat模拟一个实现Master选举过程
 * 脑裂的代码（作为课后作业）
 * 1.	持久节点（除非主动删除，zk不会做清理工作）
 * 2.	临时节点（随着session会话消亡，而消亡）
 * 3.	持久顺序节点
 * 4.	临时顺序节点
 */
public class ZkTomcatValve extends ValveBase {

	private static CuratorFramework client;
	// zk临时节点路径（传国玉玺）
	private final static String zkPath = "/Tomcat/ActiveLock";
	private static TreeCache cache;

	@Override
	public void invoke(Request request, Response response) throws IOException, ServletException {
		client = CuratorFrameworkFactory.builder().connectString("192.168.205.101:2181").connectionTimeoutMs(1000)
				.retryPolicy(new ExponentialBackoffRetry(1000, 3)).build();
		client.start();  //开始会话

		try {
			createZKNode(zkPath); //创建节点
		} catch (Exception e1) {
			System.out.println("=========== 夺位失败，对玉玺进行监控！");
			try {
				addZKNodeListener(zkPath);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 创建节点
	 * @param path
	 * @throws Exception
	 */
	private void createZKNode(String path) throws Exception {
		client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(path);
		System.out.println("=========== 创建成功，节点当选为皇帝（master）");
	}

	/**
	 * 添加监听
	 * @param path
	 * @throws Exception
	 */
	private void addZKNodeListener(final String path) throws Exception {
		cache = new TreeCache(client, path);
		cache.start();
		cache.getListenable().addListener(new TreeCacheListener() {
			public void childEvent(CuratorFramework client, TreeCacheEvent event) throws Exception {
				if (event.getData() != null && event.getType() == TreeCacheEvent.Type.NODE_REMOVED) {
					System.out.println("=========== 皇帝（master）挂了，赶紧抢玉玺！");
					createZKNode(path);
				}
			}
		});
		
		System.out.println("=========== 已经派间谍监控玉玺（ZK）");
	}
}
