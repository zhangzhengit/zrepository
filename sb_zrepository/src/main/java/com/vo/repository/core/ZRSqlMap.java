package com.vo.repository.core;

import com.google.common.collect.HashBasedTable;

/**
 * 存放生成的 ZR的子接口的实现类的方法对应的sql
 *
 * @author zhangzhen
 * @date 2023年6月15日
 *
 */
public class ZRSqlMap {

	/**
	 * <ZR的子接口的名称,ZR的子接口的名称里的方法名,ZR的子接口的名称里的方法名对应的sql>
	 */
	static HashBasedTable<String, String, String> table = HashBasedTable.create();

	public static void put(final String zRepositorySubClassName,final String methodName,final String sqlTemplate) {
		table.put(zRepositorySubClassName, methodName, sqlTemplate);
	}

	public static String get(final String zRepositorySubClassName,final String methodName) {
		final String sql = table.get(zRepositorySubClassName, methodName);
		return sql;
	}

}
