package com.vo;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 表示 @ZEntity 标记的类里面的某个属性为主键，暂只支持在一个字段上使用此注解，暂不支持联合主键.
 *
 * @author zhangzhen
 * @date 2023年6月15日
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD })
// FIXME 2023年9月4日 下午7:56:01 zhanghen: TODO 加一个属性值，可以指定对应的列名，然后可以类的字段名称可以和列名不一致，通过属性值指定列名
public @interface ZID {

	ZGenerationType strategy() default ZGenerationType.IDENTITY;

}
