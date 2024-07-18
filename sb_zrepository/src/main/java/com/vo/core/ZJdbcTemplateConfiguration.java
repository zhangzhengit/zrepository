package com.vo.core;

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
		System.out.println(Thread.currentThread().getName() + "\t" + LocalDateTime.now() + "\t"
				+ "ZJdbcTemplateConfiguration.jdbcTemplate()");

		return new ZJdbcTemplate();
	}

	@ZBean
	public <T> ZJdbcTemplate<T> zJdbcTemplate() {
		System.out.println(Thread.currentThread().getName() + "\t" + LocalDateTime.now() + "\t"
				+ "ZJdbcTemplateConfiguration.zJdbcTemplate()");

		return new ZJdbcTemplate();
	}

}
