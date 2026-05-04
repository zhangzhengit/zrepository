package com.vo.repository.anno;

/**
 * java.sql.Connection 的来源
 *
 * @author zhangzhen
 * @date 2024年6月15日 上午10:10:04
 *
 */
public enum ZCSourceEnum {

	/**
	 * 来自 @ZTransaction 注解控制，控制为在一个事务内的所有操作全都使用
	 * 一个相同的Connection来操作.
	 * 本程序中默认实现为
	 * springAOP或者zfAOP，统一为使用 @see ZTransactionAOP 类实现
	 */
	ZTRANSACTION,

	/**
	 * 来自连接池，直接从连接池获取
	 */
	ZCPOOL;

}
