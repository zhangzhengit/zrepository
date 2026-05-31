package vo.repository.anno;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 * 分表注解
 *
 * @author zhangzhen
 * @date 2024年11月30日 上午11:51:31
 *
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
public @interface ZSharding {

	/**
	 * 开始序号，如：1 则第一张的表示为 tableName_1
	 *
	 * @return
	 */
	int from();

	/**
	 * 结束序号，如：25 则第一张的表示为 tableName_25
	 *
	 * @return
	 */
	int to();

	/**
	 * 分表策略
	 *
	 * @return
	 */
	ZShardingStrategyEnum strategy() default ZShardingStrategyEnum.HASH;

}
