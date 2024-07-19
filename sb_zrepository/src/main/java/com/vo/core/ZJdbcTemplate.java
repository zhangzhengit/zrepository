package com.vo.core;

import java.util.List;

import com.vo.SQLEMode;
import com.vo.SU;
import com.vo.conn.Mode;

/**
 * 提供直接执行SQL的方法
 *
 * @param <T> @ZEntity注解标记的类
 *
 * @author zhangzhen
 * @date 2024年7月18日 下午9:26:41
 *
 */
public final class ZJdbcTemplate<T> {

	private static final Mode DEFAULT_MODE = Mode.WRITE;
	private static final String ZR_SUB_CLASS_NAME = ZJdbcTemplate.class.getCanonicalName();
	private final static Object[] ARG_EMTPY = {};

	/**
	 * 执行无参数的 SELECT 语句
	 *
	 * @param sql         要执行的SELECT语句，如：select name from blobt
	 * @param entityClass @ZEntity注解标记的类
	 */
	public  List<T> query(final String sql, final Class<T> entityClass) {

		final List<Object> zQuerySelect = SU.zQuerySelect(
				ZR_SUB_CLASS_NAME,
				"query", DEFAULT_MODE,
				entityClass,
				entityClass,
				SQLEMode.ORIGIN.name(), sql, ARG_EMTPY);

		return (List<T>) zQuerySelect;
	}

	/**
	 * 执行带参数的 SELECT 语句
	 *
	 * @param sql         要执行的SELECT语句，如：select name from blobt where id = ?1
	 * @param entityClass @ZEntity注解标记的类
	 * @param arg         参数数组，个数和类型按sql中的?1 ?2 ?3 的顺序来匹配
	 * @return
	 */
	public List<T> query(final String sql, final Class<T> entityClass, final Object... arg) {

		final List<Object> zQuerySelect = SU.zQuerySelect(
				ZR_SUB_CLASS_NAME,
				"query", DEFAULT_MODE,
				entityClass,
				entityClass,
				SQLEMode.ORIGIN.name(), sql, arg == null ? ARG_EMTPY : arg);

		return (List<T>) zQuerySelect;
	}

}
