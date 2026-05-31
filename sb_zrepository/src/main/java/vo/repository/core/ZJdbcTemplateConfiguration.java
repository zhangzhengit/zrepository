package vo.repository.core;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import vo.zframework.anno.ZBean;
import vo.zframework.anno.ZConfiguration;

/**
 *
 *
 * @author zhangzhen
 * @date 2024年7月18日 下午9:07:26
 *
 */
@Configuration
@ZConfiguration
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
