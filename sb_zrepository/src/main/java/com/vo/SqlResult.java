package com.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

/**
 *
 *
 * @author zhangzhen
 * @date 2023年9月5日
 *
 */
@Getter
@AllArgsConstructor
@Data
public class SqlResult {

	/**
	 * ZRepository 子类名称
	 */
	private final String zRepositorySubClassName;

	/**
	 * 方法名，如：findByUserId
	 */
	private final String methodName;

	/**
	 * 最终生成的sql，如 select * from user where user_id = ?
	 */
	private final String sqlFinal;
}
