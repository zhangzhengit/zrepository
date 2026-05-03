package com.vo.actuator;

import com.vo.anno.ZBean;
import com.vo.anno.ZConditional;
import com.vo.anno.ZConfiguration;
import com.vo.log.core.ZLog2;

/**
 *
 *
 * @author zhangzhen
 * @date 2024年6月8日 下午8:23:52
 *
 */
@ZConfiguration
public class SqlInvocationLogsConfiguration {

	private static final ZLog2 LOG = ZLog2.getInstance();

	@ZBean
	@ZConditional(value = SqlInvocationLogsConditional.class)
	public SqlInvocationLogsService service() {
		LOG.info("开始初始化SqlInvocationLogsService");

		final SqlInvocationLogsService service = new SqlInvocationLogsService();
		service.start();

		return service;
	}
}
