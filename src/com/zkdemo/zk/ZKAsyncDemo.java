package com.zkdemo.zk;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import org.apache.zookeeper.AsyncCallback;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;

class IStringCallback implements AsyncCallback.StringCallback {
	public void processResult(int rc, String path, Object ctx, String name) {
		System.out.println("create path result: [" + rc + ", " + path + "," + ctx + ", real path name: " + name);
	}
}

/**
 * 解释如何异步调用并完成回调,具体可以查看AsyncCallback里面各种异步调用情况
 */
public class ZKAsyncDemo implements Watcher {
	private static final CountDownLatch cdl = new CountDownLatch(1);

	public static void main(String[] args) throws IOException, InterruptedException, KeeperException {
		ZooKeeper zk = new ZooKeeper("192.168.205.101:2181", 5000, new ZKAsyncDemo());
		cdl.await();

		zk.create("/zk-test-", "123".getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL, new IStringCallback(),
				new String("I am context"));

		zk.create("/zk-test-", "456".getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL,
				new IStringCallback(), new String("I am context"));

		zk.create("/zk-test-", "789".getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL,
				new IStringCallback(), new String("I am context"));

		Thread.sleep(Integer.MAX_VALUE);
	}

	public void process(WatchedEvent event) {
		System.out.println("Receive watched event:" + event);
		if (KeeperState.SyncConnected == event.getState()) {
			cdl.countDown();
		}
	}
}
