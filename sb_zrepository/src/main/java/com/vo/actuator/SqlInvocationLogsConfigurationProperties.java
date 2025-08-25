package com.vo.actuator;

import com.vo.anno.ZConfigurationProperties;
import com.vo.validator.ZMax;
import com.vo.validator.ZMin;
import com.vo.validator.ZNotNull;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 *
 * @author zhangzhen
 * @date 2024年6月8日 下午8:25:24
 *
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
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
	private Boolean enable = false;

	/**
	 * 待save的队列最大容量
	 */
	@ZMin(min = 1)
	@ZMax(max = 5000)
	private Integer queueCapacity = 1000;

	/**
	 * saveAll的时间间隔，单位：秒
	 */
	@ZMin(min = 1)
	@ZMax(max = 60)
	private Integer saveIntervalSeconds = 5;

}
