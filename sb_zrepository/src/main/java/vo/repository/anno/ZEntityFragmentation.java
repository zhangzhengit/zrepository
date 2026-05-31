package vo.repository.anno;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.stereotype.Component;

/**
 * 用在 @ZEntity 标记的类上，表示此类对应的数据表是使用分表策略的
 * 由start和end两个属性决定连续的表名，
 * 如： table_1 table_2 table_3 .....
 *
 * @author zhangzhen
 * @date 2023年6月15日
 *
 */
// FIXME 2023年6月17日 下午11:34:27 zhanghen: 具体策略待定，是每个表单独配置hash、还是id等，还是配置文件统一配置
// FIXME 2023年6月17日 下午11:44:18 zhanghen: 仔细想想每个 ZRepository 里的方法怎么改写、子接口中声明式的方法需要支持到什么程度
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
public @interface ZEntityFragmentation {

	public static final int START = 1;

	/**
	 * 表名起始编号
	 *
	 * @return
	 */
//	int start() default START;

	/**
	 * 表名截止编号
	 *
	 * @return
	 *
	 */
	int end();

}
