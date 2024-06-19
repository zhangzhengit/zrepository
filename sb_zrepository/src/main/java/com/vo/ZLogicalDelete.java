package com.vo;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用在 @see @ZEntity 标记的类里的字段上，表示此字段为在执行[ZRepository.deleteById]操作时，
 * 使用逻辑删除：update xx = 1 where id = ?
 * 否则物理删除：delete where id = ?
 *
 * @author zhangzhen
 * @date 2024年6月16日 下午4:54:43
 *
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
public @interface ZLogicalDelete {

	public static final int DELETED = 1;
	public static final int NOT_DELETED = 0;

	/**
	 * 删除了的
	 *
	 * @return
	 */
	int deleted() default DELETED;

	/**
	 * 未删除的
	 *
	 * @return
	 */
	int undeleted() default NOT_DELETED;

}
