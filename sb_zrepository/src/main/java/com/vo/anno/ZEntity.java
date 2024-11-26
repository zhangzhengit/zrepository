package com.vo.anno;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.vo.conn.ZDatasourcePropertiesLoader;

/**
 * 用在数据库表对应的实体类上，表示本类各个字段与数据表字段名称和类型必须匹配。
 * 如需在本类中声明一个不与数据表对应的字段，在此字段上加入 @ZTransient 注解。
 *
 * 一、字段支持的类型有：
 * 	1、8个基本类型的包装类型
 * 	2、String
 * 	3、byte[] - 对应blob类型
 * 	4、java.sql.Date、java.util.Date、java.sql.Time、java.sql.Timestamp
 * 		分别对应DB(暂时为mysql)的 DATE、TIME、DATETIME、TIMESTAMP
 *
 *
 * 二、本注解标记的类里面所支持的(可能需要用到的)注解
 * 	1、 @ZID 标记Entity里面的一个属性为table的主键
 * 	2、 @ZTransient 表示此属性不与TABLE的字段对应，只用于java代码中的相关逻辑
 *
 *
 * @author zhangzhen
 * @date 2023年6月15日
 *
 */
// FIXME 2024年5月19日 下午9:28:07 zhangzhen: Entity 中使用java.sql.Date 还是有问题，在save和findById时的类型转换还是有问题，尤其是pgsql

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
public @interface ZEntity {


	// FIXME 2023年6月15日 下午1:45:52 zhanghen: 支持分库分表

	/**
	 * 对应的数据表名称，如：对应DB的中user表，则代码如下：
	 * @ZEntity(tableName = "user")
	 *
	 * @return
	 *
	 */
	String tableName();

	/**
	 * 此table所在的数据源，从哪个数据源读取/使用此table.
	 *
	 * 完整的文件名，如：zdatasource-sqlite.properties
	 *
	 * @return
	 */
	// FIXME 2024年5月31日 下午2:54:32 zhangzhen : 此字段是为了一个工程中引用了 repository_starter
	// 并且需要使用多个数据源时，而做的功能。注意：如果一个事务逻辑中的多个操作不在一个数据源里怎么做？是否有这种可能？
	// 如果有的话，在启动时提示都做不到吧？
	/*如下：
	 * @ZTransaction
	 * void method(){
	 * 		// a.xx		a.properties 数据源
	 * 		// b.xx		b.properties 数据源
	 * }
	 */
	String dataSourceName() default ZDatasourcePropertiesLoader.DEFAULT_DATSOURCE_NAME;
}
