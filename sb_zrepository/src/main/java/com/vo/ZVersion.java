package com.vo;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用在 @see ZEntity 标记的类里的字段上，表示此字段为乐观锁控制字段
 *
 * @author zhangzhen
 * @date 2024年6月15日 上午6:20:51
 *
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
public @interface ZVersion {

}
