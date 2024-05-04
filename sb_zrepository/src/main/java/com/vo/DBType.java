package com.vo;

import java.util.Collection;
import java.util.Set;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

/**
 * java数据类型和mysql数据类型的对应关系
 *
 * @author zhangzhen
 * @data 2024年5月3日 下午8:54:22
 *
 */
public class DBType {

	/**
	 * java > mysql
	 */
	private static final Multimap<String, String> JAVA_MYSQL = ArrayListMultimap.create();
	public static final Set<String> JAVA = Sets.newHashSet("byte", "short", "int", "long", "float", "double",
			"boolean", "char");

	public static Collection<String> getMysqlType(final String javaTypeName) {
		final Collection<String> v = JAVA_MYSQL.get(javaTypeName);
		return v;
	}

	/**
	 * java中的字段类型名称是否支持
	 *
	 * @param typeName
	 * @return
	 */
	public static boolean typeSupport(final String typeName) {
		final Collection<String> v = JAVA_MYSQL.get(typeName);
		return !v.isEmpty();
	}

	/**
	 * java类型和mysql类型是否匹配
	 *
	 * @param javaType
	 * @param mysqlType
	 * @return
	 */
	public static boolean match(final String javaType, final String mysqlType) {
		final Collection<String> v = getMysqlType(javaType);
		final boolean contains = v.contains(mysqlType);
		return contains;
	}

	static {

		JAVA_MYSQL.put("byte", "TINYINT");
		JAVA_MYSQL.put("java.lang.Byte", "TINYINT");

		JAVA_MYSQL.put("short", "TINYINT");
		JAVA_MYSQL.put("short", "SMALLINT");
		JAVA_MYSQL.put("java.lang.Short", "TINYINT");
		JAVA_MYSQL.put("java.lang.Short", "SMALLINT");

		JAVA_MYSQL.put("int", "TINYINT");
		JAVA_MYSQL.put("int", "SMALLINT");
		JAVA_MYSQL.put("int", "MEDIUMINT");
		JAVA_MYSQL.put("int", "INTEGER");
		JAVA_MYSQL.put("int", "INT");
		JAVA_MYSQL.put("java.lang.Integer", "TINYINT");
		JAVA_MYSQL.put("java.lang.Integer", "SMALLINT");
		JAVA_MYSQL.put("java.lang.Integer", "MEDIUMINT");
		JAVA_MYSQL.put("java.lang.Integer", "INTEGER");
		JAVA_MYSQL.put("java.lang.Integer", "INT");

		JAVA_MYSQL.put("long", "TINYINT");
		JAVA_MYSQL.put("long", "SMALLINT");
		JAVA_MYSQL.put("long", "INTEGER");
		JAVA_MYSQL.put("long", "INT");
		JAVA_MYSQL.put("long", "BIGINT");
		JAVA_MYSQL.put("java.lang.Long", "TINYINT");
		JAVA_MYSQL.put("java.lang.Long", "SMALLINT");
		JAVA_MYSQL.put("java.lang.Long", "INTEGER");
		JAVA_MYSQL.put("java.lang.Long", "INT");
		JAVA_MYSQL.put("java.lang.Long", "BIGINT");

		JAVA_MYSQL.put("float", "FLOAT");
		JAVA_MYSQL.put("java.lang.Float", "FLOAT");

		JAVA_MYSQL.put("double", "FLOAT");
		JAVA_MYSQL.put("double", "DOUBLE");
		JAVA_MYSQL.put("java.lang.Double", "FLOAT");
		JAVA_MYSQL.put("java.lang.Double", "DOUBLE");

		JAVA_MYSQL.put("java.math.BigDecimal", "DECIMAL");

		JAVA_MYSQL.put("boolean", "TINYINT");
		JAVA_MYSQL.put("java.lang.Boolean", "TINYINT");

		JAVA_MYSQL.put("char", "CHAR");
		JAVA_MYSQL.put("java.lang.Character", "CHAR");

		JAVA_MYSQL.put("java.lang.String", "CHAR");
		JAVA_MYSQL.put("java.lang.String", "VARCHAR");
		JAVA_MYSQL.put("java.lang.String", "TINYTEXT");
		JAVA_MYSQL.put("java.lang.String", "TEXT");
		JAVA_MYSQL.put("java.lang.String", "MEDIUMTEXT");
		JAVA_MYSQL.put("java.lang.String", "LONGTEXT");

		JAVA_MYSQL.put("byte[]", "TINYBLOB");
		JAVA_MYSQL.put("byte[]", "MEDIUMBLOB");
		JAVA_MYSQL.put("byte[]", "BLOB");
		JAVA_MYSQL.put("byte[]", "LONGBLOB");

		JAVA_MYSQL.put("java.sql.Date", "DATE");
		JAVA_MYSQL.put("java.util.Date", "DATE");
		JAVA_MYSQL.put("java.util.Date", "DATETIME");

		JAVA_MYSQL.put("java.sql.Time", "TIME");

		JAVA_MYSQL.put("java.sql.Timestamp", "DATETIME");
		JAVA_MYSQL.put("java.sql.Timestamp", "TIMESTAMP");

		// FIXME 2024年5月3日 下午9:01:52 zhangzhen: 加入Enum、Set类型

	}
}
