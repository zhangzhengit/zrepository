package vo.repository.actuator;

import vo.zframework.anno.ZConfigurationProperties;
import vo.zframework.validator.ZMax;
import vo.zframework.validator.ZMin;
import vo.zframework.validator.ZNotNull;

/**
 *
 *
 * @author zhangzhen
 * @date 2024年6月8日 下午8:25:24
 *
 */
@ZConfigurationProperties(prefix = "repository.actuator")
public class SqlInvocationLogsConfigurationProperties {

	/**
	 * 内置的统计SQL执行信息的数据源名称，次名称固定，想指定此数据源，必须用此名称
	 */
	public static final String NAME = "zdatasource_actuator.properties";

	/**
	 * 是否启用SQL统计功能
	 */
	@ZNotNull
	private boolean enable = false;

	/**
	 * 待save的队列最大容量
	 */
	@ZMin(min = 1)
	@ZMax(max = 5000)
	private int queueCapacity = 1000;

	/**
	 * saveAll的时间间隔，单位：秒
	 */
	@ZMin(min = 1)
	@ZMax(max = 60)
	private int saveIntervalSeconds = 5;

	public boolean getEnable() {
		return this.enable;
	}

	public void setEnable(final boolean enable) {
		this.enable = enable;
	}

	public int getQueueCapacity() {
		return this.queueCapacity;
	}

	public void setQueueCapacity(final int queueCapacity) {
		this.queueCapacity = queueCapacity;
	}

	public int getSaveIntervalSeconds() {
		return this.saveIntervalSeconds;
	}

	public void setSaveIntervalSeconds(final int saveIntervalSeconds) {
		this.saveIntervalSeconds = saveIntervalSeconds;
	}

	public SqlInvocationLogsConfigurationProperties(final boolean enable, final int queueCapacity,
			final int saveIntervalSeconds) {
		this.enable = enable;
		this.queueCapacity = queueCapacity;
		this.saveIntervalSeconds = saveIntervalSeconds;
	}

	public SqlInvocationLogsConfigurationProperties() {
	}

}
