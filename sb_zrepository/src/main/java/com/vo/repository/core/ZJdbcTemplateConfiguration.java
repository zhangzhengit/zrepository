package com.vo.repository.core;

import java.time.LocalDateTime;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.vo.anno.ZBean;
import com.vo.anno.ZConfiguration;

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
