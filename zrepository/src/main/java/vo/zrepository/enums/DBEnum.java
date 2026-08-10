package vo.zrepository.enums;

/**
 * 支持的DB
 *
 * @author zhangzhen
 * @data 2024年5月18日 上午12:09:52
 *
 */
public enum DBEnum {

	// FIXME 2024年5月27日 下午4:04:03 zhangzhen: sqlite问题：
	/*
	 * 	1、并且insert报错，改为非并发insert，但是多个连接时，依然报错： [SQLITE_BUSY] The database file is locked (database is locked)
	 * 		暂时改为单连接的连接池解决了。
	 * 		继续测试考虑好怎么解决：
	 * 		1、在jdbc层面处理为insert为串行操作？
	 * 		2、限制连接池必须为单连接的？
	 * 		3、允许多连接的，但执行一个连接来insert其他的来做其他操作？
	 *
	 */
	SQLITE,

	MYSQL,

	POSTGRESQL;
}
