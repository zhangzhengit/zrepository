package vo.repository.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用在 @ZEntity 里的与TABLE字段对应的Field上面，只有在java中Field名称与TBALE中column名称不一致时，才需要使用本注解。
 *
 * @author zhangzhen
 * @data 2024年5月12日 下午9:50:40
 *
 */
// FIXME 2024年5月12日 下午10:01:14 zhangzhen: 这个貌似很复杂，先想好怎么做
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD })
public @interface ZAlias {

	/**
	 * 对应TBALE里的字段名称，非必填项
	 *
	 * 不填：则按 java中Field.getName() 来对应TBALE的字段名称；
	 * 填了：则按本值来对应；
	 *
	 * @return
	 */
	String value();

}
