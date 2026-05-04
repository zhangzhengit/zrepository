package com.vo.repository.anno;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 指定多个对象执行的顺序,Integer类型，值越小越优先执行
 *
 * @author zhangzhen
 * @date 2024年6月21日 上午11:53:42
 *
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
public @interface ZOrder {

	int value() default 0;

}
