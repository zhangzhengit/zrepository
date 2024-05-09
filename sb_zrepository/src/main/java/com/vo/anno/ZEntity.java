package com.vo.anno;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用在数据库表对应的实体类上，表示本类各个字段与数据表字段名称和类型必须匹配。
 * 如需在本类中声明一个不与数据表对应的字段，在此字段上加入 @ZTransient 注解。
 *
 * @author zhangzhen
 * @date 2023年6月15日
 *
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
public @interface ZEntity {

	// FIXME 2023年6月15日 下午1:45:52 zhanghen: 支持分库分表

	/**
	 * 对应的数据表名称
	 *
	 * @return
	 *
	 */
	String tableName();

}
