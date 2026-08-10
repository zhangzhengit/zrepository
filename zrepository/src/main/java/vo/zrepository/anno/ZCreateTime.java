package vo.zrepository.anno;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用在 @see @ZEntity 标记的类里的字段上，表示此字段为在执行[ZRepository.save]操作时，
 * 如果字段值为null,则自动给此字段赋值为[当前时间]
 *
 * 支持类型为 ZCreateTimeHandler.SUPPORTED_CLASS_SET
 *
 * @author zhangzhen
 * @date 2024年6月16日 上午5:25:18
 *
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
public @interface ZCreateTime {

}
