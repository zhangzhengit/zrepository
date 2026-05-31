package vo.repository.anno;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用在 @ZEntity 标记的 日期类型的字段上，表示此字段的格式
 *
 * @author zhangzhen
 * @date 2023年9月24日
 *
 */
// FIXME 2024年6月22日 下午7:01:52 zhangzhen : 启动校验只能用在什么类型上，当前使用时只在java.util.Date类型上做了校验
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
public @interface ZDateFormat {

	ZDateFormatEnum format();

}
