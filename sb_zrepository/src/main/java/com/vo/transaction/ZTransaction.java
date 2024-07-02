package com.vo.transaction;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 注解式事务，用在方法上，方法执行成功则自动提交，否则回滚
 *
 * @author zhangzhen
 * @date 2023年6月17日
 *
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
public @interface ZTransaction {

	// FIXME 2024年7月2日 下午10:34:27 zhangzhen : 加一个 隔离级别属性 @see java.sql.Connection中的几个隔离级别
	// 然后在connection.setAutoCommit(false)之前，设置一下此隔离级别

	/**
	 * 给本注解标记的方法单独指定一个隔离级别
	 *
	 * 默认值为 DEFAULT，即：数据库默认是什么隔离级别就使用什么隔离级别，使用数据库默认的隔离级别
	 *
	 * @return
	 */
	ZIsolationEnum isolation() default ZIsolationEnum.DEFAULT;

}
