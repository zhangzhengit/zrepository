package vo.repository.actuator;

import vo.log.core.ZLog2;
import vo.zframework.anno.ZBean;
import vo.zframework.anno.ZConditional;
import vo.zframework.anno.ZConfiguration;
import vo.zframework.anno.ZOrder;

/**
 *
 *
 * @author zhangzhen
 * @date 2024年6月8日 下午8:23:52
 *
 */
@ZConfiguration
@ZOrder(value = Integer.MIN_VALUE + 4)
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
