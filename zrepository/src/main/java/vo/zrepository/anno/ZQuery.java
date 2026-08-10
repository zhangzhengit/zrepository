package vo.zrepository.anno;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import vo.zrepository.enums.DMLEnum;

/**
 *
 * 自定义SQL，用在ZRepository的子接口的方法上，方法上有本注解，则优先按自定义SQL来查询，不按[声明式方法]来查询
 *
 * 如果不设置sql属性，则默认为从resources/mapper目录读取和ZRepository子接口同名的xml文件中的Method.name的select标签
 * 如：
 *
 * 	public interface BlobRepository extends ZRepository<BlobEntity, Integer> {
		@ZQuery
		List<BlobEntity> selectMapperById(Integer id);
	}

	上面的[selectMapperById]方法没有声明sql属性，
	则表示读取 resources/mapper目录下的xml文件中id=selectMapperById的select标签

	声明xml文件书写内容如下：

	BlobRepository.xml

	<?xml version="1.0" encoding="UTF-8"?>
	<mapper>
		<select id="selectMapperById">
			SELECT 	*
			FROM
				blobt
			WHERE
				id = ?1;
		</select>
	</mapper>

	或者直接声明sql属性，如下：

	public interface BlobRepository extends ZRepository<BlobEntity, Integer> {
		@ZQuery(sql="SELECT * FROM	blobt WHERE	id = ?1;")
		List<BlobEntity> selectMapperById(Integer id);
	}

	则会优先使用使用上面这种注解中直接声明的SQL

 *
 *
 * select：返回List<T> 如：
 *
 * 		@ZQuery(sql = "select name from blobt limit ?;")
 * 		List<NameEntity> selectNameLimitN(Integer n);
 *		NameLengthEntity 中必须有 SQL的字段，如上的name属性，否则查出数据Field值为null
 *
 * update : 返回Integer，update的行数 如：
 *		@ZQuery(sql = "update blobt set name = ? where id = ?") Integer
 *             updateNameById(String name,Integer id);
 *
 * 自定义SQL需要AS时，为兼容PGSQL，AS后面请使用下划线命名法，否则可能导致SQL匹配
 * 不到returnType中的Field，如：
 * 		SELECT MAX(id) AS max_id
 *
 * 或者 Max_ID / max_ID / max_Id / MAX_id / MAX_ID 等等形式都可以
 * 只要是下划线命名法就可以，代码中已经处理为toLowerCase然后才匹配returnType中的Field(本例中为maxId)了
 *
 * @author zhangzhen
 * @date 2023年6月16日
 *
 */
// FIXME 2024年5月9日 下午11:30:48 zhangzhen: 到此为止，只简单测试了select 和update的 1 2 3 4 个参数的方法，继续测试：

// FIXME 2024年5月10日 下午9:07:42 zhangzhen: MYSQL 不支持 Array array = connection.createArrayOf("VARCHAR", valueArray) 这个操作，报错 SQLFeatureNotSupportedException？
//通义说的，这行代码是chatgpt给我的

// FIXME 2024年5月12日 下午8:43:07 zhangzhen: 自定义 select where xx in (?) 即上面createArrayOf的的问题。还有问题

/**
 *
 *
 * @author zhangzhen
 * @date 2024年7月2日 下午3:21:29
 *
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
public @interface ZQuery {

	public static final String SELECT_METHOD_NAME_PREFIX = "SELECT";
	public static final String INSERT_METHOD_NAME_PREFIX = "INSERT";
	public static final String UPDATE_METHOD_NAME_PREFIX = "UPDATE";
	public static final String DELETE_METHOD_NAME_PREFIX = "DELETE";

	public static final String MAPPER = "ZRMAPPER";

	/**
	 * 自定义SQL
	 * 不指定此值:则表示从resources/mapper下和ZRepository子接口同名的xml文件
	 * 中寻找对应本注解标记的Method的名称同名的<select>标签中取SQL。
	 * 指定了此值:则直接用此值作为SQL。
	 *
	 * 注意：上面两者同时存在，则优先使用代码中本注解的此值作为SQL
	 *
	 * @return
	 */
	String sql() default MAPPER;

	/**
	 * 操作
	 *
	 * @return
	 */
	DMLEnum dml() default DMLEnum.SELECT;

}
