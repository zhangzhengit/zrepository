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
// FIXME 2024年5月10日 下午10:18:36 zhangzhen: mysql float 类型 where column = value 查不出数据
// 考虑怎么做，是限制用mysql时ZEntity不允许使用float？还是在jdbc层面处理？

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
