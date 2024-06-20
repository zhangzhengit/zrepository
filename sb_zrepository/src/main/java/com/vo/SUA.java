package com.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 *
 * @author zhangzhen
 * @date 2024年6月18日 下午3:42:01
 *
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SUA {

	Class<?> entityClass;
	Object entityObject;

	Class<?> returnClass;
	String sql;
	Object[] arg;

	/**
	 * hanlder处理后返回的部分where 条件，如： name = ?
	 */
	private String where;

	public SUA(final Class<?> entityClass, final Object entityObject, final Class<?> returnClass, final String sql, final Object[] arg) {
		this.entityClass = entityClass;
		this.entityObject = entityObject;
		this.returnClass = returnClass;
		this.sql = sql;
		this.arg = arg;
	}

}
