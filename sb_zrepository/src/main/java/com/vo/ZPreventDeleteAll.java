package com.vo;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *	用在 @ZEntity 标记的类上，表示此类在执行 [ZRepository.deleteAll] 时，
 *	判断是否带了条件，如果不带条件则抛出异常，防止代码编写手误等而删除了全部数据
 *
 * @author zhangzhen
 * @date 2024年6月21日 上午11:23:38
 *
 */
// FIXME 2024年6月21日 上午11:25:16 zhangzhen : 写这个
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
public @interface ZPreventDeleteAll {

}
