package vo.repository.actuator;

import vo.vortex.anno.ZCondition;
import vo.vortex.anno.ZConfigurationPropertiesRegistry;

/**
 *
 *
 * @author zhangzhen
 * @date 2024年6月8日 下午8:24:50
 *
 */
public class SqlInvocationLogsConditional implements ZCondition {

	@Override
	public boolean matches(final ZConfigurationPropertiesRegistry configurationPropertiesRegistry) {
		final SqlInvocationLogsConfigurationProperties sqlcp = (SqlInvocationLogsConfigurationProperties) configurationPropertiesRegistry
				.getConfigurationPropertie(SqlInvocationLogsConfigurationProperties.class);
		final Boolean enable = sqlcp.getEnable();
		return Boolean.TRUE.equals(enable);
	}

}
