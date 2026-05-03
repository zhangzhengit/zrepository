package com.vo.actuator;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.vo.core.ZContext;
import com.vo.log.core.ZLog2;

/**
 *
 *
 * @author zhangzhen
 * @date 2024年6月8日 下午8:23:31
 *
 */
public class SqlInvocationLogsService extends Thread {

	private static final ZLog2 LOG = ZLog2.getInstance();

	private final BlockingQueue<SqlInvocationLogsEntity> queue = new LinkedBlockingQueue<>(
			ZContext.getBean(SqlInvocationLogsConfigurationProperties.class).getQueueCapacity());

	public void add(final SqlInvocationLogsEntity entity) {

		try {
			while (this.queue.size() >= ZContext.getBean(SqlInvocationLogsConfigurationProperties.class)
					.getQueueCapacity()) {
				this.queue.poll();
			}
			this.queue.add(entity);
		} catch (final Exception e) {
			// 忽略 queue full
		}
	}

	@Override
	public void run() {
		LOG.info("启动SqlInvocationLogs-saveAllJob");

		while (true) {

			final List<SqlInvocationLogsEntity> list = new ArrayList<>(this.queue);
			final SqlInvocationLogsRepository r = ZContext.getBean(SqlInvocationLogsRepository.class);
			try {
				r.saveAll(list);
				this.queue.clear();
			} catch (final Exception e) {
				final String message = com.vo.core.Task.gExceptionMessage(e);
				LOG.error("SqlInvocationLogs-saveAllJob-saveAll异常-continue,message={}", message);
				continue;
			}

			try {
				Thread.sleep(ZContext.getBean(SqlInvocationLogsConfigurationProperties.class).getSaveIntervalSeconds()
						* 1000);
			} catch (final InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

}
