package com.vo;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用在 @see @ZEntity 标记的类里的字段上，表示此字段为在执行[SAVE]操作时，
 * 自动给此字段赋值[当前时间],支持类型为java.util.Date/java.sql.Timestamp
 *
 * @author zhangzhen
 * @date 2024年6月16日 上午5:25:18
 *
 */
// FIXME 2024年6月16日 上午4:39:45 zhangzhen : 写这个，启动校验类型，
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
public @interface ZCreateTime {

}
