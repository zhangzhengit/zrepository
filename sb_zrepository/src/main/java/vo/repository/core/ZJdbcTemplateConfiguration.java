package vo.repository.core;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import vo.zframework.anno.ZBean;
import vo.zframework.anno.ZConfiguration;
import vo.zframework.anno.ZOrder;

/**
 *
 *
 * @author zhangzhen
 * @date 2024年7月18日 下午9:07:26
 *
 */
@Configuration
@ZConfiguration
@ZOrder(value = Integer.MIN_VALUE + 2)
public class ZJdbcTemplateConfiguration {

	@Bean
	public <T> ZJdbcTemplate<T> jdbcTemplate() {
		return new ZJdbcTemplate();
	}

	@ZBean
	public <T> ZJdbcTemplate<T> zJdbcTemplate() {
		return new ZJdbcTemplate();
	}

}
