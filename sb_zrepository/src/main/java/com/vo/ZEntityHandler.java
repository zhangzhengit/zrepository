package com.vo;

/**
 * @ZEntity 的字段处理器
 *
 *          需要自定义处理某些字段，则扩展本类即可
 *
 * @author zhangzhen
 * @date 2024年6月16日 上午4:45:22
 *
 */
public interface ZEntityHandler {

	/**
	 * 对字段做的处理
	 *
	 * @param entityObject @ZEntity标记的对象
	 */
	void handle(Object entityObject);

	/**
	 * 是否符合某个条件
	 *
	 * @param entityClass @ZEntity标记的Class
	 * @return
	 */
	boolean condition(Class<?> entityClass);

}
