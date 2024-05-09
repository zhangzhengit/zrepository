package com.vo;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 * 自定义SQL，用在ZRepository的子接口的方法上，方法上有本注解，则优先按自定义SQL来查询，不按[声明式方法]来查询
 *
 * select：返回List<T> 如：
 * 		@ZQuery(sql = "select name  from blobt limit ?;")
		List<NameLengthEntity> selectNameLimitN(Integer n);

		NameLengthEntity 中声明有 String name属性;
		调用	selectNameLimitN(5)即可获取前5条的name值
 *
 * @author zhangzhen
 * @date 2023年6月16日
 *
 */
// FIXME 2024年5月9日 下午10:41:46 zhangzhen: 到此为止，只简单测试了select 方法，继续测试：
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD })
public @interface ZQuery {

	// FIXME 2023年6月16日 下午8:03:51 zhanghen: 解析这个
	/**
	 * 自定义SQL
	 *
	 * @return
	 */
	// FIXME 2024年5月5日 下午10:22:47 zhangzhen: 先支持完整的原生sql
	String sql();

}
