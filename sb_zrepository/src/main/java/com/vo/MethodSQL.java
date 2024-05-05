package com.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 声明式/自定义方法 解析的SQL
 *
 * @author zhangzhen
 * @data 2024年5月5日 下午10:12:34
 *
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MethodSQL {

	/**
	 * 是否 @ZQuery 自定义SQL的方法
	 */
	private boolean isZQuery;

	/**
	 * 声明式方法名称/自定义方法名称
	 */
	private String methodName;

	/**
	 * SQL模板/自定义SQL
	 */
	private String sqlTemplate;
}
