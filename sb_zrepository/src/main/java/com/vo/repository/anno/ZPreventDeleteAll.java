package com.vo.repository.anno;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用在 @ZEntity 标记的类上，仅在执行 [ZRepository.deleteAll] 时，
 * 此注解起作用：阻止不带条件的deleteAll，包括逻辑删除和物理删除。
 *
 * 即：阻止 [ZRepository.deleteAll] 操作，因为 [ZRepository.deleteAll]
 * 就是不带任务条件的(WHERE 1 = 1这种不算)。
 *
 * 如果就是需要[ZRepository.deleteAll]操作，则不要使用本注解
 *
 * @author zhangzhen
 * @date 2024年6月21日 上午11:23:38
 *
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
public @interface ZPreventDeleteAll {

}
