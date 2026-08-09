package vo.repository.core;

import vo.vortex.anno.ZBean;
import vo.vortex.anno.ZConfiguration;
import vo.vortex.anno.ZOrder;

/**
 *
 *
 * @author zhangzhen
 * @date 2024年7月18日 下午9:07:26
 *
 */
//@Configuration
@ZConfiguration
@ZOrder(value = Integer.MIN_VALUE + 2)
public class ZJdbcTemplateConfiguration {

//	@Bean
//	public <T> ZJdbcTemplate<T> jdbcTemplate() {
//		return new ZJdbcTemplate();
//	}

	@ZBean
	public <T> ZJdbcTemplate<T> zJdbcTemplate() {
		return new ZJdbcTemplate();
	}

}
