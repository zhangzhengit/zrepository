package com.vo.anno;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.stereotype.Component;

/**
 * 用在数据库表对应的实体类
 *
 * @author zhangzhen
 * @date 2023年6月15日
 *
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })


// FIXME 2024年5月4日 下午3:37:28 zhangzhen: TODO 考虑：本注解的标记的类仅作为对应数据表用？这样的话，就严格限制为其中所有字段都与column一一对应，不允许出现其他任何字段，
// 但这样可能不太合理;
// 或者提供一个注解，表示此字段不与column对应，只存在于代码中有其他用途？

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
