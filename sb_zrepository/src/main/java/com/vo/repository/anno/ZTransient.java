package com.vo.repository.anno;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用在 @ZEntity 标记的类里面的字段上，表示此属性不与TABLE的字段对应，只用于java代码中的相关逻辑
 *
 * @author zhangzhen
 * @data 2024年5月9日 下午11:55:31
 *
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD })
public @interface ZTransient {

}
