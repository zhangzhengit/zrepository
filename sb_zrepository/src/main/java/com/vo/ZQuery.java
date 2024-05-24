package com.vo;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 * 自定义SQL，用在ZRepository的子接口的方法上，方法上有本注解，则优先按自定义SQL来查询，不按[声明式方法]来查询

  　select：返回List<T> 如：
  		@ZQuery(sql = "select name from blobt limit ?;")
		List<NameEntity> selectNameLimitN(Integer n);

		NameLengthEntity 中必须有 SQL的字段，如上的name属性

	update : 返回Integer，update的行数 如：
 		@ZQuery(sql = "update blobt set name = ? where id = ?")
		Integer updateNameById(String name,Integer id);


 * @author zhangzhen
 * @date 2023年6月16日
 *
 */
// FIXME 2024年5月9日 下午11:30:48 zhangzhen: 到此为止，只简单测试了select 和update的 1 2 3 4 个参数的方法，继续测试：

// FIXME 2024年5月10日 下午9:07:42 zhangzhen: MYSQL 不支持 Array array = connection.createArrayOf("VARCHAR", valueArray) 这个操作，报错 SQLFeatureNotSupportedException？
//通义说的，这行代码是chatgpt给我的

// FIXME 2024年5月12日 下午8:43:07 zhangzhen: 自定义 select where xx in (?) 即上面createArrayOf的的问题。还有问题

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD })
// FIXME 2024年5月18日 上午1:06:03 zhangzhen: pgsql遇到的问题2：
// @ZQuery(sql = "select length(name) as nameLength from blobt limit ?;")
// nameLength 会在jdbc中取到namelength,导致java class.getDField时获取不到，而mysqljdbc获取到的就是 nameLength

// FIXME 2024年5月24日 下午7:03:18 zhangzhen: 考虑好：对于非SQL标准的内容，比如pgslq的jsonb类型，是否在模板方法上支持？比如save方法，
//  insert 要做成 ?::josnb ，一旦模板方法支持了，其他的所有声明式方法可能都要改动，工作量太大。还是使用@ZQuery 算了，本来不打算支持 @ZQuery insert 语句
// 现在看来对于使用一些特有功能还是直接让手写sql算了.

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
