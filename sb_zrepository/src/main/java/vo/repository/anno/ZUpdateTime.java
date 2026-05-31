package vo.repository.anno;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用在 @see @ZEntity 标记的类里的字段上，表示此字段为在执行[ZRepository.update]操作时，
 * 直接忽略字段值，给此字段赋值为[当前时间]
 *
 * 支持类型为 ZUpdateTimeHandler.SUPPORTED_CLASS_SET
 *
 * @author zhangzhen
 * @date 2024年6月16日 上午4:38:59
 *
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
public @interface ZUpdateTime {

}
