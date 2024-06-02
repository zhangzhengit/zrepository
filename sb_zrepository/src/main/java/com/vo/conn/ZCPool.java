package com.vo.conn;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.vo.conn.ZDatasourceProperties.P;
import com.vo.core.ZLog2;

import cn.hutool.core.util.StrUtil;
import lombok.Getter;

/**
 *
 * 数据库连接池
 *
 * @author zhangzhen
 * @date 2023年6月15日
 *
 */
public class ZCPool {

	private static final ZLog2 LOG = ZLog2.getInstance();
	private final Vector<ZConnection> writeVector = new java.util.Vector<>();
	private final Vector<ZConnection> readVector = new java.util.Vector<>();

	private final AtomicInteger writeI = new AtomicInteger();

	private final AtomicInteger readI = new AtomicInteger();

	@Getter
	private String dataSourceName = null;

	private final Object readLock = new Object();
	private final Object writeLock = new Object();

	private static final AtomicBoolean addShutdownHook = new AtomicBoolean(false);

	private void initialize(final String dataSourceName) {

		this.create(dataSourceName);

		final ZCPoolJob job = new ZCPoolJob();
		job.start();

		if (!addShutdownHook.get()) {

			Runtime.getRuntime().addShutdownHook(new Thread(() -> {
				try {
					Thread.sleep(100);
				} catch (final InterruptedException e) {
					e.printStackTrace();
				}

				LOG.info("JVM钩子执行:开始关闭数据源");
				this.shutdown();
				LOG.info("JVM钩子已成功关闭数据源");
			}));
			addShutdownHook.set(true);
		}

	}

	static HashMap<String, ZCPool> poolMap = Maps.newHashMap();

	public static Collection<ZCPool> getAllInstance() {
		return poolMap.values();
	}

	public static ZCPool getInstance(final String dataSourceName) {
		if (StrUtil.isEmpty(dataSourceName)) {
			throw new IllegalArgumentException("dataSourceName 不能为空");
		}

		final ZCPool pool = poolMap.get(dataSourceName);
		if (pool != null) {
			return pool;
		}

		synchronized (dataSourceName.intern()) {
			final ZCPool newPool = new ZCPool();
			newPool.dataSourceName = dataSourceName;
			newPool.initialize(dataSourceName);
			poolMap.put(dataSourceName, newPool);
			return newPool;
		}

	}

	void incrementWriteI() {
		synchronized (this.incrementLock) {
			final int g = this.writeI.incrementAndGet();
			if (g >= this.writeVector.size()) {
				this.writeI.set(0);
			}
		}
	}

	void incrementReadI() {
		synchronized (this.incrementLock) {
			final int g = this.readI.incrementAndGet();
			if (g >= this.readVector.size()) {
				this.readI.set(0);
			}
		}
	}

	Object incrementLock = new Object();

	/**
	 * 轮询获取连接对象，暂定为轮询获取，不管当前连接是否忙碌，轮询获取
	 *
	 * @param mode TODO
	 *
	 * @return
	 *
	 */
	public ZConnection getZConnection(final Mode mode) {

		if (mode == Mode.WRITE) {

			return this.getWRITE();

		}

		if (mode == Mode.READ) {

			return this.getREAD();

		}

		throw new IllegalArgumentException("mode 错误");
	}

	private ZConnection getREAD() {

		final int ms = 1000 * 10;
		synchronized (this.readLock) {

			for (int i = 1; i <= ms; i++) {
				final Optional<ZConnection> findFirst = this.readVector.stream().filter(zc -> !zc.getBusy()).findFirst();
				if (findFirst.isPresent()) {
					final ZConnection zc = findFirst.get();
					zc.setBusy(true);
					return zc;
				}

				try {
					this.readLock.wait(1);
				} catch (final InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		throw new IllegalArgumentException("获取不到空闲的[读]连接");
	}

	private ZConnection getWRITE() {
		// FIXME 2023年9月6日 上午2:36:59 zhanghen: ms 配置为参数
		final int ms = 1000 * 10;
		synchronized (this.writeLock) {

			for (int i = 1; i <= ms; i++) {
				final Optional<ZConnection> findFirst = this.writeVector.stream().filter(zc -> !zc.getBusy())
						.findFirst();
				if (findFirst.isPresent()) {
					final ZConnection zc = findFirst.get();
					zc.setBusy(true);
					return zc;
				}
				try {
					this.writeLock.wait(1);
				} catch (final InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

		throw new IllegalArgumentException("获取不到空闲的[写]连接");
	}

	/**
	 * 归还一个连接
	 *
	 * @param zConnection
	 *
	 */
	public void returnZConnectionAndCommit(final ZConnection zConnection) {

		final Mode mode = zConnection.getMode();
		switch (mode) {
		case WRITE:
			this.returnWrite(zConnection);
			break;

		case READ:
			this.returnRead(zConnection);
			break;

		default:
			break;
		}

	}

	private void returnRead(final ZConnection zConnection) {
		synchronized (this.readLock) {

			for (final ZConnection zc : this.readVector) {
				if (zc.getConnection() == zConnection.getConnection()) {
					try {
						zConnection.getConnection().commit();
					} catch (final SQLException e) {
						e.printStackTrace();
					}
					zc.setBusy(false);
					this.readLock.notify();
					break;
				}
			}
		}
	}


	private void returnWrite(final ZConnection zConnection) {
		synchronized (this.writeLock) {

			for (final ZConnection zc : this.writeVector) {
				if (zc.getConnection() == zConnection.getConnection()) {
					try {
						// FIXME 2024年5月21日 下午3:07:13 zhangzhen: 测试出的问题：当server(armbian的panther x2 mysql-8.0.34-0ubuntu0.22.04.1)硬盘满了，commit 会一直卡着没反应，也不报错，也没法设置超时时间，怎么办？
						zConnection.getConnection().commit();
					} catch (final SQLException e) {
						e.printStackTrace();
					}
					zc.setBusy(false);
					this.writeLock.notify();
					break;
				}
			}
		}
	}

	/**
	 * 移除一个连接
	 *
	 * @param zConnection
	 *
	 */
	public void removeZConnection(final ZConnection zConnection) {
		LOG.warn("开始删除一个连接ZConnection={}", zConnection);

		final Optional<ZConnection> findAnyWRITE = this.writeVector.stream()
				.filter(zc -> zc.getConnection() == zConnection.getConnection()).findAny();
		if (findAnyWRITE.isPresent()) {
			this.writeVector.remove(findAnyWRITE.get());
			LOG.warn("成功删除一个[写]连接ZConnection={}", zConnection);
		} else {
			final Optional<ZConnection> findAnyREAD = this.readVector.stream()
					.filter(zc -> zc.getConnection() == zConnection.getConnection()).findAny();
			if (findAnyREAD.isPresent()) {
				this.readVector.remove(findAnyREAD.get());
				LOG.warn("成功删除一个[读]连接ZConnection={}", zConnection);
			}
		}
	}

	/**
	 * 获取全部的连接，包括写连接和读连接
	 *
	 * @return
	 *
	 */
	public ImmutableList<ZConnection> getAll() {
		final List<ZConnection> r = Lists.newArrayList(this.writeVector);
		r.addAll(this.readVector);
		return ImmutableList.copyOf(r);
	}

	private void create(final String dataSourceName) {
		LOG.info("开始初始化数据源,properties文件名称=[{}]", dataSourceName);
		final ZDatasourceProperties zdp = ZDatasourcePropertiesLoader.getInstance(dataSourceName);

		final P write = zdp.getWrite();
		this.newWriteConnection(write);

		final Integer datasourceReadUrlCount = zdp.getDatasourceReadUrlCount();
		final List<P> r = zdp.getReadList();
		for (int i = 0; i < datasourceReadUrlCount; i++) {
			this.newReadConnection(r.get(i));
		}

		LOG.info("初始化数据源完成,properties文件名称=[{}],ZDatasourceProperties={}", dataSourceName, zdp);
	}

	private synchronized void newReadConnection(final ZDatasourceProperties.P p) {
		final String url = p.getDatasourceUrl();
		// FIXME 2023年6月16日 下午12:35:04 zhanghen:先暂时处理为从1到max
		final int minConnection = 1;
		//		 final Integer minConnection = p.getDatasourceMinConnection();
		final Integer maxConnection = p.getDatasourceMaxConnection();

		LOG.info("开始建立数据库[读]连接,min={},max={},url={}", minConnection, maxConnection, url);

		for (int i = minConnection; i <= maxConnection; i++) {
			final ZConnection zConnection = ZConnection.newConnection(p);
			zConnection.setMode(Mode.READ);
			this.readVector.add(zConnection);
			LOG.info("第{}个数据库[读]连接创建成功,ZConnection={}", i, zConnection);
			LOG.info("当前已创建[读]连接数={}", this.readVector.size());
		}
	}

	private synchronized void newWriteConnection(final ZDatasourceProperties.P p) {
		final String url = p.getDatasourceUrl();
		// FIXME 2023年6月16日 下午12:35:04 zhanghen:先暂时处理为从1到max
		final int minConnection = 1;
		//		 final Integer minConnection = p.getDatasourceMinConnection();
		final Integer maxConnection = p.getDatasourceMaxConnection();

		LOG.info("开始建立数据库[写]连接,min={},max={},url={}", minConnection, maxConnection, url);

		for (int i = minConnection; i <= maxConnection; i++) {
			final ZConnection zConnection = ZConnection.newConnection(p);
			zConnection.setMode(Mode.WRITE);
			this.writeVector.add(zConnection);
			LOG.info("第{}个数据库[写]连接创建成功,ZConnection={}", i, zConnection);
		}
	}
	// FIXME 2024年6月1日 上午3:16:29 zhangzhen : shutdown 此方法 从 poolMap 取值然后关闭
	private synchronized void shutdown() {
		final Set<String> keySet = poolMap.keySet();
		LOG.info("开始关闭数据源:当前[{}]个数据源-[{}]", keySet.size(), keySet);
		int c = 0;
		for (final String k : keySet) {
			final ZCPool pool = poolMap.get(k);
			ZCPool.close(pool.writeVector);
			ZCPool.close(pool.readVector);
			c += (pool.writeVector.size() + pool.readVector.size());
		}

		LOG.info("成功关闭[{}]个数据源里的[{}]个连接",keySet.size(),c);
	}

	private static void close(final Vector<ZConnection> writeVector2) {
		for (final ZConnection zConnection : writeVector2) {
			final Connection c = zConnection.getConnection();
			try {
				final boolean closed = c.isClosed();
				if (!closed) {
					c.close();
				}
			} catch (final SQLException e) {
				e.printStackTrace();
			}
		}
	}

}
