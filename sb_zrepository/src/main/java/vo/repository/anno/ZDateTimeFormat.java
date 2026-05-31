package vo.repository.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用在 @ZEntity 里的日期/时间类型的字段上，限制字段的格式，存入DB时，按本注解的格式存入；
 *
 * 从DB取出时，也处理为本注解的格式
 *
 * @author zhangzhen
 * @data 2024年5月12日 下午10:05:06
 *
 */
// FIXME 2024年5月12日 下午10:17:42 zhangzhen: java中自定义格式再多再丰富，
// mysql 中日期/时间类型也只支持那一两种，这个注解是否必要的？
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD })
public @interface ZDateTimeFormat {

	String YYYY_MM_DD = "yyyy-MM-dd";

	/**
	 * 指定的日期格式，支持选项看上面的常量值
	 *
	 * @return
	 */
	String pattern();

}
