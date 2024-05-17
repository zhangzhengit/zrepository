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
// FIXME 2024年5月18日 上午12:28:42 zhangzhen: 注意，到此为止，写死的类型关系，
// 只限于 mysql-8.0.34-0ubuntu0.22.04.1
// 和pgsql-PostgreSQL 14.11 (Ubuntu 14.11-0ubuntu0.22.04.1) on aarch64-unknown-linux-gnu, compiled by gcc (Ubuntu 11.4.0-1ubuntu1~22.04) 11.4.0, 64-bit
// 并且也只写了几个常见类型，其他类型和版本要继续支持

public class DBType {

	// FIXME 2024年5月13日 上午12:34:27 zhangzhen: TODO aliyun 已启动 pgsql，开始写pgsql相关的

	/**
	 * java > pgsql
	 */
	private static final Multimap<String, String> JAVA_PGSQL = ArrayListMultimap.create();
	/**
	 * java > mysql
	 */
	private static final Multimap<String, String> JAVA_MYSQL = ArrayListMultimap.create();
	public static final Set<String> JAVA = Sets.newHashSet("byte", "short", "int", "long", "float", "double",
			"boolean", "char");

	public static Collection<String> getPGSqlType(final String javaTypeName) {
		final Collection<String> v = JAVA_PGSQL.get(javaTypeName);
		return v;
	}

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
	 * java类型和DB类型是否匹配
	 *
	 * @param javaType
	 * @param dbType
	 * @return
	 */
	public static boolean match(final String javaType, final String dbType) {
		final DBEnum db = ZRepositoryMain.getDB();
		if (db == DBEnum.MYSQL) {
			return getMysqlType(javaType).contains(dbType);
		}
		if (db == DBEnum.POSTGRESQL) {
 			return getPGSqlType(javaType).contains(dbType);
		}

		return false;
	}

	static {

		// java -> pgsql
		JAVA_PGSQL.put("java.lang.Byte", "smallint");
		JAVA_PGSQL.put("java.lang.Byte", "integer");
		JAVA_PGSQL.put("java.lang.Byte", "int4");

		JAVA_PGSQL.put("java.lang.Short", "smallint");
		JAVA_PGSQL.put("java.lang.Short", "integer");
		JAVA_PGSQL.put("java.lang.Short", "int4");
		JAVA_PGSQL.put("java.lang.Integer", "int4");
		JAVA_PGSQL.put("java.lang.Integer", "integer");
		JAVA_PGSQL.put("java.lang.Long", "int8");
		JAVA_PGSQL.put("java.lang.Long", "bigint");
		JAVA_PGSQL.put("java.lang.Character", "bpchar");
		JAVA_PGSQL.put("java.lang.Character", "char");
		JAVA_PGSQL.put("java.lang.Character", "character");
		JAVA_PGSQL.put("java.lang.Float", "float8");
		JAVA_PGSQL.put("java.lang.Float", "real");
		JAVA_PGSQL.put("java.lang.Double", "numeric");
		JAVA_PGSQL.put("java.lang.Double", "double precision");
		JAVA_PGSQL.put("java.math.BigDecimal", "numeric");
		JAVA_PGSQL.put("java.lang.String", "varchar");
		JAVA_PGSQL.put("java.lang.String", "text");
		JAVA_PGSQL.put("java.lang.Boolean", "bool");
		JAVA_PGSQL.put("java.lang.Boolean", "boolean");
		JAVA_PGSQL.put("java.sql.Date", "timestamp");
		JAVA_PGSQL.put("java.sql.Date", "date");
		JAVA_PGSQL.put("java.util.Date", "timestamp");
		JAVA_PGSQL.put("java.util.Date", "date");
		JAVA_PGSQL.put("java.sql.Time", "time");
		JAVA_PGSQL.put("java.sql.Timestamp", "timestamp");
		JAVA_PGSQL.put("byte[]", "bytea");
		JAVA_PGSQL.put("java.lang.Object", "jsonb");


		// java -> mysql
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
