package vo.repository.core;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import vo.repository.anno.ZEntity;
import vo.repository.conn.Mode;
import vo.repository.conn.SU;
import vo.repository.conn.ZCPool;
import vo.repository.conn.ZRepositoryMain;
import vo.repository.enums.DBEnum;
import vo.repository.enums.SQLOperatorEnum;
import vo.repository.exception.ZRepositoryException;
import vo.zframework.core.ZRC;

/**
 * 查询条件,使用方法引用来动态构造查询条件.
 * 方法自动忽略null值,如：
 * 	w.eq(BlobEntity::getName, null);
 * 生成：
 * 	where 1=1
 *
 * 	w.eq(BlobEntity::getName, "zhangsan");
 * 生成：
 * 	where name = 'zhangsan'
 *
 * 如需指定查询字段值为null/不为null的，
 * 用 isNull/notNull方法，如：
 * 	w.isNull(BlobEntity::getName);
 * 生成：
 * 	where name IS NULL
 *
 * 	w.notNull(BlobEntity::getName);
 * 生成：
 * 	where name IS NOT NULL
 *
 *
 * 同一个本类对象调用除了and和or以外的方法，默认是AND关系，如：
 *
 * 		final ZRWrapper<BlobEntity> eq = ZRWrapper.wrap(BlobEntity.class);
 *		eq.startingWith(BlobEntity::getName, "zhangsan");
 *		eq.isNull(BlobEntity::getChar1);
 *		eq.lt(BlobEntity::getCreateTime,new Date());
 *
 * 生成条件为：
 * 		WHERE name like '?%' AND char1 IS NULL AND create_time < ?
 * eq.后面加再多条件，也同为AND关系，可以不用and方法组合，同一个对象本就是AND关系的多个条件.
 *
 * 如需OR关系，需要再多构造一个OR的本类对象：
 *
 * 		final ZRWrapper<BlobEntity> eq = ZRWrapper.wrap(BlobEntity.class);
 *		eq.startingWith(BlobEntity::getName, "zhangsan");
 *		eq.isNull(BlobEntity::getChar1);
 *		eq.lt(BlobEntity::getCreateTime,new Date());
 *
 *  	final ZRWrapper<BlobEntity> or = ZRWrapper.wrap(BlobEntity.class);
 *  	or.eq(BlobEntity::getShort1, s2.getShort1());
 *		or.eq(BlobEntity::getLong1, s2.getLong1());
 *  	eq.or(or);
 *
 * 生成条件为：
 * 		WHERE (name like '?%' AND char1 IS NULL AND create_time < ?)
 * 			OR (short1 = ? AND long1 = ?)
 *
 * 举例：
 *
 * 单查询条件（单个本类构造的查询条件）：
 *
 * 查询：name = zhangsan
 * 		final ZRWrapper<BlobEntity> eq = ZRWrapper.wrap(BlobEntity.class);
 *		eq.eq(BlobEntity::getName, "zhangsan");
 *
 *		生成WHERE：WHERE name = '?'
 *
 * 查询：name = zhangsan 并且 id <= 200
 *		final ZRWrapper<BlobEntity> eq = ZRWrapper.wrap(BlobEntity.class);
 *		eq.eq(BlobEntity::getName, "zhangsan");
 *		eq.lte(BlobEntity::getId,200);
 *
 *		生成WHERE：WHERE name = '?' AND id <= ?
 *
 * 查询：name 以张三开头 并且  char1 为空 并且 createTime 小于某个时间点
 * 		final ZRWrapper<BlobEntity> eq = ZRWrapper.wrap(BlobEntity.class);
 *		eq.startingWith(BlobEntity::getName, "zhangsan");
 *		eq.isNull(BlobEntity::getChar1);
 *		eq.lt(BlobEntity::getCreateTime,new Date());
 *
 *		生成WHERE：WHERE name like '?%' AND char IS NULL AND create_time < ?
 *
 * 多查询条件（多个本类对象组合出的查询条件）：
 *
 * 查询： 条件1：name 不为空 并且 id 在某个范围内 并且 time 小于某个时间点
 * 	 或者 条件2：byte1 为空 并且 long1 不等于 3
 *		final ZRWrapper<BlobEntity> eq = ZRWrapper.wrap(BlobEntity.class);
		eq.notNull(BlobEntity::getName);
		eq.between(BlobEntity::getId, 200, 5000);
		eq.lt(BlobEntity::getTime, new Date());

		final ZRWrapper<BlobEntity> or = ZRWrapper.wrap(BlobEntity.class);
		or.isNull(BlobEntity::getByte1);
		or.ne(BlobEntity::getLong1, 3);

		eq.or(or);

		生成WHERE：

			WHERE
			((
				NAME IS NOT NULL
					AND id BETWEEN ? AND ?
					AND time < '?'
					)
				OR ((
						byte1 IS NULL
					AND long1 != ?
			)))

 *
 * @param <T>	T 为需要用到的自定义ZRepository中的第一个泛型类型，如：
  	自定义ZRepository：
  		public interface BlobRepository extends ZRepository<BlobEntity, Integer> {}

  	构造 BlobRepository.find需要用到的本类对象，则T为BlobEntity,构造方式为：

  		final ZRWrapper<BlobEntity> eq = ZRWrapper.wrap(BlobEntity.class);
		eq.notNull(BlobEntity::getName);
		eq.between(BlobEntity::getId, 200, 5000);
		final List<BlobEntity> find = this.blobRepository.find(eq);

	构造方式，使用wrap方法来构造：
		final ZRWrapper<BlobEntity> eq = ZRWrapper.wrap(BlobEntity.class);

 * @author zhangzhen
 * @date 2024年6月12日 下午7:32:13
 *
 */
// FIXME 2024年6月29日 下午11:21:18 zhangzhen : 考虑：连 max min 这种最简单的函数也不打算支持了，
// 即使支持这种最简单的函数，本类也要大改，因为支持了or操作。哪怕支持稍复杂一点的avg函数，都很可能
// [avg(integer) 结果是double] 这种情况导致 entityClass 赋值异常，ZR.find 又是固定的返回类型，不好搞
// 目前就只支持自定义 where / order by / limit
public class ZRWrapper<T> {

	private static final int ONE = 1;
	private static final int ZERO = 0;
	public static final String ALWAYS_TRUE = " 1 = 1 ";
	public static final String SPACE = " ";
	private static final String AND = MethodRegex.AND;
	private static final String OR = MethodRegex.OR;

	public static final String NULL = "NULL";

	/**
	 * 存放由各个方法传值而来构造的WHERE条件
	 */
	private final List<String> where = new ArrayList<>();
	private final List<String> orderBy = new ArrayList<>();

	private final List<String> limit = new ArrayList<>();

	private final Class<T> entityClass;


	/**
	 * 构造一个本类对象，Class 为 @ZEntity 标记的类
	 *
	 * @param <T>
	 * @param entityClass
	 * @return
	 */
	public static <T> ZRWrapper<T> wrap(final Class<T> entityClass) {
		if (entityClass == null) {
			throw new NullPointerException("entityClass不能为空");
		}

		if (!entityClass.isAnnotationPresent(ZEntity.class)) {
			throw new ZRepositoryException(
					"entityClass必须是 @" + ZEntity.class.getName() + " 标记的类，当前为 " + entityClass);
		}

		final ZRWrapper<T> w = new ZRWrapper<>(entityClass);
		return w;
	}

	private ZRWrapper(final Class<T> entityClass) {
		this.orderBy.add(Sort.ORDER_BY);
		this.entityClass = entityClass;
	}

	/**
	 * 等值,构造条件如: name = ?
	 * 调用本方法的方式为：
	 * 		wrapper.eq(MyEntity::getName(),myEntity.getName());
	 * 或者
	 * 		wrapper.eq(MyEntity::getName(),"张三李四");
	 *
	 * @param function
	 * @param value
	 * @return
	 */
	public ZRWrapper<T> eq(final SerializableFunction<T, Object> function, final Byte value) {
		return this.addValue0(function, value, SQLOperatorEnum.EQ);
	}

	public ZRWrapper<T> eq(final SerializableFunction<T, Object> function, final Short value) {
		return this.addValue0(function, value, SQLOperatorEnum.EQ);
	}

	public ZRWrapper<T> eq(final SerializableFunction<T, Object> function, final Integer value) {
		return this.addValue0(function, value, SQLOperatorEnum.EQ);
	}

	public ZRWrapper<T> eq(final SerializableFunction<T, Object> function, final Long value) {
		return this.addValue0(function, value, SQLOperatorEnum.EQ);
	}

	public ZRWrapper<T> eq(final SerializableFunction<T, Object> function, final Double value) {
		return this.addValue0(function, value, SQLOperatorEnum.EQ);
	}

	public ZRWrapper<T> eq(final SerializableFunction<T, Object> function, final BigDecimal value) {
		return this.addValue0(function, value, SQLOperatorEnum.EQ);
	}

	public ZRWrapper<T> eq(final SerializableFunction<T, Object> function, final Boolean value) {
		return this.addValue0(function, value, SQLOperatorEnum.EQ);
	}

	public ZRWrapper<T> eq(final SerializableFunction<T, Object> function, final Character value) {
		return this.addValue0(function, value, SQLOperatorEnum.EQ);
	}

	public ZRWrapper<T> eq(final SerializableFunction<T, Object> function, final String value) {
		return this.addValue0(function, value, SQLOperatorEnum.EQ);
	}

	public ZRWrapper<T> eq(final SerializableFunction<T, Object> function, final java.sql.Date value) {
		return this.addValue0(function, value, SQLOperatorEnum.EQ);
	}

	public ZRWrapper<T> eq(final SerializableFunction<T, Object> function, final java.util.Date value) {
		return this.addValue0(function, value, SQLOperatorEnum.EQ);
	}

	public ZRWrapper<T> eq(final SerializableFunction<T, Object> function, final java.sql.Time value) {
		return this.addValue0(function, value, SQLOperatorEnum.EQ);
	}

	public ZRWrapper<T> eq(final SerializableFunction<T, Object> function, final Timestamp value) {
		return this.addValue0(function, value, SQLOperatorEnum.EQ);
	}

	public ZRWrapper<T> eq(final SerializableFunction<T, Object> function, final LocalDate value) {
		return this.addValue0(function, value, SQLOperatorEnum.EQ);
	}

	public ZRWrapper<T> eq(final SerializableFunction<T, Object> function, final LocalTime value) {
		return this.addValue0(function, value, SQLOperatorEnum.EQ);
	}

	public ZRWrapper<T> eq(final SerializableFunction<T, Object> function, final LocalDateTime value) {
		return this.addValue0(function, value, SQLOperatorEnum.EQ);
	}

	/**
	 * 不等值,构造条件如: name != ?
	 * 调用本方法的方式为：
	 * 		wrapper.ne(MyEntity::getName(),myEntity.getName());
	 * 或者
	 * 		wrapper.ne(MyEntity::getName(),"张三李四");
	 *
	 * @param function
	 * @param value
	 * @return
	 */
	public ZRWrapper<T> ne(final SerializableFunction<T, Object> function, final Byte value) {
		return this.addValue0(function, value, SQLOperatorEnum.NE);
	}

	public ZRWrapper<T> ne(final SerializableFunction<T, Object> function, final Short value) {
		return this.addValue0(function, value, SQLOperatorEnum.NE);
	}

	public ZRWrapper<T> ne(final SerializableFunction<T, Object> function, final Integer value) {
		return this.addValue0(function, value, SQLOperatorEnum.NE);
	}

	public ZRWrapper<T> ne(final SerializableFunction<T, Object> function, final Long value) {
		return this.addValue0(function, value, SQLOperatorEnum.NE);
	}

	public ZRWrapper<T> ne(final SerializableFunction<T, Object> function, final Double value) {
		return this.addValue0(function, value, SQLOperatorEnum.NE);
	}

	public ZRWrapper<T> ne(final SerializableFunction<T, Object> function, final BigDecimal value) {
		return this.addValue0(function, value, SQLOperatorEnum.NE);
	}

	public ZRWrapper<T> ne(final SerializableFunction<T, Object> function, final Boolean value) {
		return this.addValue0(function, value, SQLOperatorEnum.NE);
	}

	public ZRWrapper<T> ne(final SerializableFunction<T, Object> function, final String value) {
		return this.addValue0(function, value, SQLOperatorEnum.NE);
	}

	public ZRWrapper<T> ne(final SerializableFunction<T, Object> function, final Character value) {
		return this.addValue0(function, value, SQLOperatorEnum.NE);
	}

	public ZRWrapper<T> ne(final SerializableFunction<T, Object> function, final Date value) {
		return this.addValue0(function, value, SQLOperatorEnum.NE);
	}

	public ZRWrapper<T> ne(final SerializableFunction<T, Object> function, final java.sql.Date value) {
		return this.addValue0(function, value, SQLOperatorEnum.NE);
	}

	public ZRWrapper<T> ne(final SerializableFunction<T, Object> function, final java.sql.Time value) {
		return this.addValue0(function, value, SQLOperatorEnum.NE);
	}

	public ZRWrapper<T> ne(final SerializableFunction<T, Object> function, final java.sql.Timestamp value) {
		return this.addValue0(function, value, SQLOperatorEnum.NE);
	}

	public ZRWrapper<T> ne(final SerializableFunction<T, Object> function, final LocalDate value) {
		return this.addValue0(function, value, SQLOperatorEnum.NE);
	}

	public ZRWrapper<T> ne(final SerializableFunction<T, Object> function, final LocalTime value) {
		return this.addValue0(function, value, SQLOperatorEnum.NE);
	}

	public ZRWrapper<T> ne(final SerializableFunction<T, Object> function, final LocalDateTime value) {
		return this.addValue0(function, value, SQLOperatorEnum.NE);
	}


	/**
	 * 小于,构造条件如: id < ?
	 * 调用本方法的方式为：
	 * 		wrapper.lt(MyEntity::getId(),myEntity.getId());
	 * 或者
	 * 		wrapper.lt(MyEntity::getId(),200);
	 *
	 * @param function
	 * @param value
	 * @return
	 */
	public ZRWrapper<T> lt(final SerializableFunction<T, Object> function, final Byte value) {
		return this.addValue0(function, value, SQLOperatorEnum.LT);
	}

	public ZRWrapper<T> lt(final SerializableFunction<T, Object> function, final Short value) {
		return this.addValue0(function, value, SQLOperatorEnum.LT);
	}

	public ZRWrapper<T> lt(final SerializableFunction<T, Object> function, final Integer value) {
		return this.addValue0(function, value, SQLOperatorEnum.LT);
	}

	public ZRWrapper<T> lt(final SerializableFunction<T, Object> function, final Long value) {
		return this.addValue0(function, value, SQLOperatorEnum.LT);
	}

	public ZRWrapper<T> lt(final SerializableFunction<T, Object> function, final Double value) {
		return this.addValue0(function, value, SQLOperatorEnum.LT);
	}

	public ZRWrapper<T> lt(final SerializableFunction<T, Object> function, final BigDecimal value) {
		return this.addValue0(function, value, SQLOperatorEnum.LT);
	}

	public ZRWrapper<T> lt(final SerializableFunction<T, Object> function, final Boolean value) {
		return this.addValue0(function, value, SQLOperatorEnum.LT);
	}

	public ZRWrapper<T> lt(final SerializableFunction<T, Object> function, final String value) {
		return this.addValue0(function, value, SQLOperatorEnum.LT);
	}

	public ZRWrapper<T> lt(final SerializableFunction<T, Object> function, final Character value) {
		return this.addValue0(function, value, SQLOperatorEnum.LT);
	}

	// FIXME 2024年6月14日 下午8:22:36 zhangzhen : 考虑所有方法要不要严格判断：value类型？
	// 因为：pgsql中测试 lt(blobEntity::getTime(),new java.util.Date()) 执行sql报错：
	// ND time < 'Fri Jun 14 20:22:25 CST 2024' 而 pgsql 中time类型格式为 20:22:25
	// 所以：要不要在本类中就及时提示可能的问题（类型不匹配等）
	public ZRWrapper<T> lt(final SerializableFunction<T, Object> function, final Date value) {
		return this.addValue0(function, value, SQLOperatorEnum.LT);
	}

	public ZRWrapper<T> lt(final SerializableFunction<T, Object> function, final java.sql.Date value) {
		return this.addValue0(function, value, SQLOperatorEnum.LT);
	}

	public ZRWrapper<T> lt(final SerializableFunction<T, Object> function, final java.sql.Time value) {
		return this.addValue0(function, value, SQLOperatorEnum.LT);
	}

	public ZRWrapper<T> lt(final SerializableFunction<T, Object> function, final java.sql.Timestamp value) {
		return this.addValue0(function, value, SQLOperatorEnum.LT);
	}

	public ZRWrapper<T> lt(final SerializableFunction<T, Object> function, final LocalDateTime value) {
		return this.addValue0(function, value, SQLOperatorEnum.LT);
	}

	public ZRWrapper<T> lt(final SerializableFunction<T, Object> function, final LocalDate value) {
		return this.addValue0(function, value, SQLOperatorEnum.LT);
	}

	public ZRWrapper<T> lt(final SerializableFunction<T, Object> function, final LocalTime value) {
		return this.addValue0(function, value, SQLOperatorEnum.LT);
	}

	/**
	 * 小于等于,构造条件如: id <= ? 调用本方法的方式为：
	 * wrapper.lte(MyEntity::getId(),myEntity.getId()); 或者
	 * wrapper.lte(MyEntity::getId(),200);
	 *
	 * @param function
	 * @param value
	 * @return
	 */
	public ZRWrapper<T> lte(final SerializableFunction<T, Object> function, final Byte value) {
		return this.addValue0(function, value, SQLOperatorEnum.LTE);
	}

	public ZRWrapper<T> lte(final SerializableFunction<T, Object> function, final Short value) {
		return this.addValue0(function, value, SQLOperatorEnum.LTE);
	}

	public ZRWrapper<T> lte(final SerializableFunction<T, Object> function, final Integer value) {
		return this.addValue0(function, value, SQLOperatorEnum.LTE);
	}

	public ZRWrapper<T> lte(final SerializableFunction<T, Object> function, final Long value) {
		return this.addValue0(function, value, SQLOperatorEnum.LTE);
	}

	public ZRWrapper<T> lte(final SerializableFunction<T, Object> function, final Double value) {
		return this.addValue0(function, value, SQLOperatorEnum.LTE);
	}

	public ZRWrapper<T> lte(final SerializableFunction<T, Object> function, final BigDecimal value) {
		return this.addValue0(function, value, SQLOperatorEnum.LTE);
	}

	public ZRWrapper<T> lte(final SerializableFunction<T, Object> function, final Boolean value) {
		return this.addValue0(function, value, SQLOperatorEnum.LTE);
	}

	public ZRWrapper<T> lte(final SerializableFunction<T, Object> function, final String value) {
		return this.addValue0(function, value, SQLOperatorEnum.LTE);
	}

	public ZRWrapper<T> lte(final SerializableFunction<T, Object> function, final Character value) {
		return this.addValue0(function, value, SQLOperatorEnum.LTE);
	}

	public ZRWrapper<T> lte(final SerializableFunction<T, Object> function, final Date value) {
		return this.addValue0(function, value, SQLOperatorEnum.LTE);
	}

	public ZRWrapper<T> lte(final SerializableFunction<T, Object> function, final java.sql.Date value) {
		return this.addValue0(function, value, SQLOperatorEnum.LTE);
	}

	public ZRWrapper<T> lte(final SerializableFunction<T, Object> function, final java.sql.Time value) {
		return this.addValue0(function, value, SQLOperatorEnum.LTE);
	}

	public ZRWrapper<T> lte(final SerializableFunction<T, Object> function, final java.sql.Timestamp value) {
		return this.addValue0(function, value, SQLOperatorEnum.LTE);
	}

	public ZRWrapper<T> lte(final SerializableFunction<T, Object> function, final LocalDateTime value) {
		return this.addValue0(function, value, SQLOperatorEnum.LTE);
	}

	public ZRWrapper<T> lte(final SerializableFunction<T, Object> function, final LocalDate value) {
		return this.addValue0(function, value, SQLOperatorEnum.LTE);
	}

	public ZRWrapper<T> lte(final SerializableFunction<T, Object> function, final LocalTime value) {
		return this.addValue0(function, value, SQLOperatorEnum.LTE);
	}

	/**
	 * 大于,构造条件如: id > ?
	 * 调用本方法的方式为：
	 * 		wrapper.gt(MyEntity::getId(),myEntity.getId());
	 * 或者
	 * 		wrapper.gt(MyEntity::getId(),200);
	 *
	 * @param function
	 * @param value
	 * @return
	 */
	public ZRWrapper<T> gt(final SerializableFunction<T, Object> function, final Byte value) {
		return this.addValue0(function, value, SQLOperatorEnum.GT);
	}

	public ZRWrapper<T> gt(final SerializableFunction<T, Object> function, final Short value) {
		return this.addValue0(function, value, SQLOperatorEnum.GT);
	}

	public ZRWrapper<T> gt(final SerializableFunction<T, Object> function, final Integer value) {
		return this.addValue0(function, value, SQLOperatorEnum.GT);
	}

	public ZRWrapper<T> gt(final SerializableFunction<T, Object> function, final Long value) {
		return this.addValue0(function, value, SQLOperatorEnum.GT);
	}

	public ZRWrapper<T> gt(final SerializableFunction<T, Object> function, final Double value) {
		return this.addValue0(function, value, SQLOperatorEnum.GT);
	}

	public ZRWrapper<T> gt(final SerializableFunction<T, Object> function, final BigDecimal value) {
		return this.addValue0(function, value, SQLOperatorEnum.GT);
	}

	public ZRWrapper<T> gt(final SerializableFunction<T, Object> function, final Boolean value) {
		return this.addValue0(function, value, SQLOperatorEnum.GT);
	}

	public ZRWrapper<T> gt(final SerializableFunction<T, Object> function, final String value) {
		return this.addValue0(function, value, SQLOperatorEnum.GT);
	}

	public ZRWrapper<T> gt(final SerializableFunction<T, Object> function, final Character value) {
		return this.addValue0(function, value, SQLOperatorEnum.GT);
	}

	public ZRWrapper<T> gt(final SerializableFunction<T, Object> function, final Date value) {
		return this.addValue0(function, value, SQLOperatorEnum.GT);
	}

	public ZRWrapper<T> gt(final SerializableFunction<T, Object> function, final java.sql.Date value) {
		return this.addValue0(function, value, SQLOperatorEnum.GT);
	}

	public ZRWrapper<T> gt(final SerializableFunction<T, Object> function, final java.sql.Time value) {
		return this.addValue0(function, value, SQLOperatorEnum.GT);
	}

	public ZRWrapper<T> gt(final SerializableFunction<T, Object> function, final java.sql.Timestamp value) {
		return this.addValue0(function, value, SQLOperatorEnum.GT);
	}

	public ZRWrapper<T> gt(final SerializableFunction<T, Object> function, final LocalDateTime value) {
		return this.addValue0(function, value, SQLOperatorEnum.GT);
	}

	public ZRWrapper<T> gt(final SerializableFunction<T, Object> function, final LocalDate value) {
		return this.addValue0(function, value, SQLOperatorEnum.GT);
	}

	public ZRWrapper<T> gt(final SerializableFunction<T, Object> function, final LocalTime value) {
		return this.addValue0(function, value, SQLOperatorEnum.GT);
	}

	/**
	 * 大于等于,构造条件如: id >= ?
	 * 调用本方法的方式为：
	 * 		wrapper.lte(MyEntity::getId(),myEntity.getId());
	 * 或者
	 * 		wrapper.lte(MyEntity::getId(),200);
	 *
	 * @param function
	 * @param value
	 * @return
	 */
	public ZRWrapper<T> gte(final SerializableFunction<T, Object> function, final Byte value) {
		return this.addValue0(function, value, SQLOperatorEnum.GTE);
	}

	public ZRWrapper<T> gte(final SerializableFunction<T, Object> function, final Short value) {
		return this.addValue0(function, value, SQLOperatorEnum.GTE);
	}

	public ZRWrapper<T> gte(final SerializableFunction<T, Object> function, final Integer value) {
		return this.addValue0(function, value, SQLOperatorEnum.GTE);
	}

	public ZRWrapper<T> gte(final SerializableFunction<T, Object> function, final Long value) {
		return this.addValue0(function, value, SQLOperatorEnum.GTE);
	}

	public ZRWrapper<T> gte(final SerializableFunction<T, Object> function, final Double value) {
		return this.addValue0(function, value, SQLOperatorEnum.GTE);
	}

	public ZRWrapper<T> gte(final SerializableFunction<T, Object> function, final BigDecimal value) {
		return this.addValue0(function, value, SQLOperatorEnum.GTE);
	}

	public ZRWrapper<T> gte(final SerializableFunction<T, Object> function, final Boolean value) {
		return this.addValue0(function, value, SQLOperatorEnum.GTE);
	}

	public ZRWrapper<T> gte(final SerializableFunction<T, Object> function, final String value) {
		return this.addValue0(function, value, SQLOperatorEnum.GTE);
	}

	public ZRWrapper<T> gte(final SerializableFunction<T, Object> function, final Character value) {
		return this.addValue0(function, value, SQLOperatorEnum.GTE);
	}

	public ZRWrapper<T> gte(final SerializableFunction<T, Object> function, final Date value) {
		return this.addValue0(function, value, SQLOperatorEnum.GTE);
	}

	public ZRWrapper<T> gte(final SerializableFunction<T, Object> function, final java.sql.Date value) {
		return this.addValue0(function, value, SQLOperatorEnum.GTE);
	}

	public ZRWrapper<T> gte(final SerializableFunction<T, Object> function, final Time value) {
		return this.addValue0(function, value, SQLOperatorEnum.GTE);
	}

	public ZRWrapper<T> gte(final SerializableFunction<T, Object> function, final Timestamp value) {
		return this.addValue0(function, value, SQLOperatorEnum.GTE);
	}

	public ZRWrapper<T> gte(final SerializableFunction<T, Object> function, final LocalDateTime value) {
		return this.addValue0(function, value, SQLOperatorEnum.GTE);
	}

	public ZRWrapper<T> gte(final SerializableFunction<T, Object> function, final LocalDate value) {
		return this.addValue0(function, value, SQLOperatorEnum.GTE);
	}

	public ZRWrapper<T> gte(final SerializableFunction<T, Object> function, final LocalTime value) {
		return this.addValue0(function, value, SQLOperatorEnum.GTE);
	}

	/**
	 * 模糊查询,构造条件如: name LIKE '%?%'
	 *
	 * 调用本方法的方式为：
	 * 		wrapper.like(MyEntity::getName(),myEntity.getName());
	 * 或者
	 * 		wrapper.like(MyEntity::getName(),"张三李四");
	 *
	 * @param function
	 * @param value
	 * @return
	 */
	public ZRWrapper<T> like(final SerializableFunction<T, Object> function, final String value) {
		return this.addValue0(function, value, SQLOperatorEnum.LIKE);
	}

	public ZRWrapper<T> like(final SerializableFunction<T, Object> function, final Character value) {
		return this.addValue0(function, value, SQLOperatorEnum.LIKE);
	}

	/**
	 * NOT 模糊查询,构造条件如: name NOT LIKE '%?%'
	 *
	 * 调用本方法的方式为：
	 * 		wrapper.notLike(MyEntity::getName(),myEntity.getName());
	 * 或者
	 * 		wrapper.notLike(MyEntity::getName(),"张三李四");
	 *
	 * @param function
	 * @param value
	 * @return
	 */
	public ZRWrapper<T> notLike(final SerializableFunction<T, Object> function, final String value) {
		return this.addValue0(function, value, SQLOperatorEnum.NOT_LIKE);
	}

	public ZRWrapper<T> notLike(final SerializableFunction<T, Object> function, final Character value) {
		return this.addValue0(function, value, SQLOperatorEnum.NOT_LIKE);
	}

	/**
	 * 判断column is null,构造条件如: name IS NUll
	 *
	 * 调用本方法的方式为：
	 * 		wrapper.isNull(MyEntity::getName());
	 *
	 * @param function
	 * @param value
	 * @return
	 */
	public ZRWrapper<T> isNull(final SerializableFunction<T, Object> function) {
		return this.addValue0(function, null, SQLOperatorEnum.IS_NULL);
	}

	/**
	 * 判断column IS NOT NULL,构造条件如: name IS NOT NUll
	 *
	 * 调用本方法的方式为：
	 * 		wrapper.notNull(MyEntity::getName());
	 *
	 * @param function
	 * @param value
	 * @return
	 */
	public ZRWrapper<T> notNull(final SerializableFunction<T, Object> function) {
		return this.addValue0(function, null, SQLOperatorEnum.NOT_NULL);
	}

	/**
	 * 后缀匹配查询,构造条件如: name LIKE '%?'
	 *
	 * 调用本方法的方式为：
	 * 		wrapper.endingWith(MyEntity::getName(),myEntity.getName());
	 * 或者
	 * 		wrapper.endingWith(MyEntity::getName(),"张三李四");
	 *
	 * @param function
	 * @param value
	 * @return
	 */
	public ZRWrapper<T> endingWith(final SerializableFunction<T, Object> function, final String value) {
		return this.addValue0(function, value, SQLOperatorEnum.ENDING_WITH);
	}

	public ZRWrapper<T> endingWith(final SerializableFunction<T, Object> function, final Character value) {
		return this.addValue0(function, value, SQLOperatorEnum.ENDING_WITH);
	}

	/**
	 * 前缀匹配查询,构造条件如: name LIKE '?%'
	 *
	 * 调用本方法的方式为：
	 * 		wrapper.startingWith(MyEntity::getName(),myEntity.getName());
	 * 或者
	 * 		wrapper.startingWith(MyEntity::getName(),"张三李四");
	 *
	 * @param function
	 * @param value
	 * @return
	 */
	public ZRWrapper<T> startingWith(final SerializableFunction<T, Object> function, final String value) {
		return this.addValue0(function, value, SQLOperatorEnum.STARTING_WITH);
	}

	public ZRWrapper<T> startingWith(final SerializableFunction<T, Object> function, final Character value) {
		return this.addValue0(function, value, SQLOperatorEnum.STARTING_WITH);
	}

	/**
	 * IN查询,构造条件如: name IN (?,?,?)
	 *
	 * 调用本方法的方式为：
	 * 		wrapper.in(MyEntity::getId,Lists.newArrayList(e1.getId(),e2.getId(),e3.getId()));
	 * 或者
	 * 		wrapper.in(MyEntity::getId,Lists.newArrayList(1,2,3));
	 *
	 * @param function
	 * @param iterable	传值多个条件值，类型为Iterable,传值为null或者空的Iterable对象,则直接忽略对应的条件字段
	 * @return
	 */
	public ZRWrapper<T> in(final SerializableFunction<T, Object> function, final Iterable<?> iterable) {
		final Field f = ReflectionUtil.getField(function);
		final boolean array = f.getType().isArray();
		if (array) {
			throw new UnsupportedOperationException("数组类型不支持:" + iterable);
		}

		if (iterable == null) {
			return this.addValue0(function, null, SQLOperatorEnum.IN);
		}
		final Iterator<?> it = iterable.iterator();
		if (!it.hasNext()) {
			return this.addValue0(function, null, SQLOperatorEnum.IN);
		}

		return this.addValue0(function, iterable, SQLOperatorEnum.IN);
	}

	/**
	 * NOT IN查询,构造条件如: name NOT IN (?,?,?)
	 *
	 * 调用本方法的方式为：
	 * 		wrapper.notIn(MyEntity::getId,Lists.newArrayList(e1.getId(),e2.getId(),e3.getId()));
	 * 或者
	 * 		wrapper.notIn(MyEntity::getId,Lists.newArrayList(1,2,3));
	 *
	 * @param function
	 * @param iterable	传值多个条件值，类型为Iterable,传值为null或者空的Iterable对象,则直接忽略对应的条件字段
	 * @return
	 */
	public ZRWrapper<T> notIn(final SerializableFunction<T, Object> function, final Iterable<?> iterable) {
		final Field f = ReflectionUtil.getField(function);
		final boolean array = f.getType().isArray();
		if (array) {
			throw new UnsupportedOperationException("数组类型不支持:" + iterable);
		}

		if (iterable == null) {
			return this.addValue0(function, null, SQLOperatorEnum.NOT_IN);
		}
		final Iterator<?> it = iterable.iterator();
		if (!it.hasNext()) {
			return this.addValue0(function, null, SQLOperatorEnum.NOT_IN);
		}

		return this.addValue0(function, iterable, SQLOperatorEnum.NOT_IN);
	}

	/**
	 * BETWEEN 范围查询,构造条件如: name BETWEEN ? AND ?
	 *
	 * 调用本方法的方式为：
	 * 		wrapper.between(MyEntity::getId,e1.getId(),e2.getId());
	 * 或者
	 * 		wrapper.between(MyEntity::getId,1,2);
	 *
	 * 两个参数都为null，则直接忽略对应的字段条件：
	 * 		wrapper.between(MyEntity::getId,null,null);
	 * 		wrapper.eq(MyEntity::getName,"zhangsan");
	 *
	 * 会直接忽略掉id条件，生成where：
	 * 		where name = 'zhangsan'
	 *
	 * 前面参数为null：
	 * 		wrapper.between(MyEntity::getId,null,200);
	 * 		wrapper.eq(MyEntity::getName,"zhangsan");
	 *
	 * 生成：
	 * 		where id BETWEEN NULL AND 200 AND name = 'zhangsan'
	 *
	 * 后面参数为null：
	 * 		wrapper.between(MyEntity::getId,200,null);
	 * 		wrapper.eq(MyEntity::getName,"zhangsan");
	 *
	 * 生成：
	 * 		where id BETWEEN 200 AND NULL AND name = 'zhangsan'
	 *
	 *
	 * @param function
	 * @param value1
	 * @param value2
	 * @return
	 */
	public ZRWrapper<T> between(final SerializableFunction<T, Object> function, final Integer value1,
			final Integer value2) {
		if ((value1 == null) && (value2 == null)) {
			return this.addValue0(function, null, SQLOperatorEnum.BETWEEN);
		}

		final Object[] a = { value1, value2 };
		return this.addValue0(function, a, SQLOperatorEnum.BETWEEN);
	}

	public ZRWrapper<T> between(final SerializableFunction<T, Object> function, final Double value1,
			final Double value2) {
		if ((value1 == null) && (value2 == null)) {
			return this.addValue0(function, null, SQLOperatorEnum.BETWEEN);
		}

		final Object[] a = { value1, value2 };
		return this.addValue0(function, a, SQLOperatorEnum.BETWEEN);
	}

	public ZRWrapper<T> between(final SerializableFunction<T, Object> function, final java.sql.Date value1,
			final java.sql.Date value2) {
		if ((value1 == null) && (value2 == null)) {
			return this.addValue0(function, null, SQLOperatorEnum.BETWEEN);
		}

		final Object[] a = { value1, value2 };
		return this.addValue0(function, a, SQLOperatorEnum.BETWEEN);
	}

	public ZRWrapper<T> between(final SerializableFunction<T, Object> function, final LocalDateTime value1,
			final LocalDateTime value2) {
		if ((value1 == null) && (value2 == null)) {
			return this.addValue0(function, null, SQLOperatorEnum.BETWEEN);
		}

		final Object[] a = { value1, value2 };
		return this.addValue0(function, a, SQLOperatorEnum.BETWEEN);
	}

	public ZRWrapper<T> between(final SerializableFunction<T, Object> function, final LocalDate value1,
			final LocalDate value2) {
		if ((value1 == null) && (value2 == null)) {
			return this.addValue0(function, null, SQLOperatorEnum.BETWEEN);
		}

		final Object[] a = { value1, value2 };
		return this.addValue0(function, a, SQLOperatorEnum.BETWEEN);
	}

	public ZRWrapper<T> between(final SerializableFunction<T, Object> function, final LocalTime value1,
			final LocalTime value2) {
		if ((value1 == null) && (value2 == null)) {
			return this.addValue0(function, null, SQLOperatorEnum.BETWEEN);
		}

		final Object[] a = { value1, value2 };
		return this.addValue0(function, a, SQLOperatorEnum.BETWEEN);
	}

	public ZRWrapper<T> between(final SerializableFunction<T, Object> function, final Date value1,
			final Date value2) {
		if ((value1 == null) && (value2 == null)) {
			return this.addValue0(function, null, SQLOperatorEnum.BETWEEN);
		}

		final Object[] a = { value1, value2 };
		return this.addValue0(function, a, SQLOperatorEnum.BETWEEN);
	}

	public ZRWrapper<T> between(final SerializableFunction<T, Object> function, final Timestamp value1,
			final Timestamp value2) {
		if ((value1 == null) && (value2 == null)) {
			return this.addValue0(function, null, SQLOperatorEnum.BETWEEN);
		}

		final Object[] a = { value1, value2 };
		return this.addValue0(function, a, SQLOperatorEnum.BETWEEN);
	}

	public ZRWrapper<T> between(final SerializableFunction<T, Object> function, final Time value1,
			final Time value2) {
		if ((value1 == null) && (value2 == null)) {
			return this.addValue0(function, null, SQLOperatorEnum.BETWEEN);
		}

		final Object[] a = { value1, value2 };
		return this.addValue0(function, a, SQLOperatorEnum.BETWEEN);
	}

	public ZRWrapper<T> between(final SerializableFunction<T, Object> function, final Boolean value1,
			final Boolean value2) {
		if ((value1 == null) && (value2 == null)) {
			return this.addValue0(function, null, SQLOperatorEnum.BETWEEN);
		}

		final Object[] a = { value1, value2 };
		return this.addValue0(function, a, SQLOperatorEnum.BETWEEN);
	}

	public ZRWrapper<T> between(final SerializableFunction<T, Object> function, final Short value1,
			final Short value2) {
		if ((value1 == null) && (value2 == null)) {
			return this.addValue0(function, null, SQLOperatorEnum.BETWEEN);
		}

		final Object[] a = { value1, value2 };
		return this.addValue0(function, a, SQLOperatorEnum.BETWEEN);
	}

	public ZRWrapper<T> between(final SerializableFunction<T, Object> function, final Character value1,
			final Character value2) {
		if ((value1 == null) && (value2 == null)) {
			return this.addValue0(function, null, SQLOperatorEnum.BETWEEN);
		}

		final Object[] a = { value1, value2 };
		return this.addValue0(function, a, SQLOperatorEnum.BETWEEN);
	}

	public ZRWrapper<T> between(final SerializableFunction<T, Object> function, final BigDecimal value1,
			final BigDecimal value2) {
		if ((value1 == null) && (value2 == null)) {
			return this.addValue0(function, null, SQLOperatorEnum.BETWEEN);
		}

		final Object[] a = { value1, value2 };
		return this.addValue0(function, a, SQLOperatorEnum.BETWEEN);
	}

	public ZRWrapper<T> between(final SerializableFunction<T, Object> function, final String value1,
			final String value2) {
		if ((value1 == null) && (value2 == null)) {
			return this.addValue0(function, null, SQLOperatorEnum.BETWEEN);
		}

		final Object[] a = { value1, value2 };
		return this.addValue0(function, a, SQLOperatorEnum.BETWEEN);
	}

	/**
	 * NOT BETWEEN 范围查询,构造条件如: name NOT BETWEEN ? AND ?
	 *
	 * 调用本方法的方式为：
	 * 		wrapper.notBetween(MyEntity::getId,e1.getId(),e2.getId());
	 * 或者
	 * 		wrapper.notBetween(MyEntity::getId,1,2);
	 *
	 * 两个参数都为null，则直接忽略对应的字段条件：
	 * 		wrapper.notBetween(MyEntity::getId,null,null);
	 * 		wrapper.eq(MyEntity::getName,"zhangsan");
	 *
	 * 会直接忽略掉id条件，生成where：
	 * 		where name = 'zhangsan'
	 *
	 * 前面参数为null：
	 * 		wrapper.notBetween(MyEntity::getId,null,200);
	 * 		wrapper.eq(MyEntity::getName,"zhangsan");
	 *
	 * 生成：
	 * 		where id NOT BETWEEN NULL AND 200 AND name = 'zhangsan'
	 *
	 * 后面参数为null：
	 * 		wrapper.notBetween(MyEntity::getId,200,null);
	 * 		wrapper.eq(MyEntity::getName,"zhangsan");
	 *
	 * 生成：
	 * 		where id NOT BETWEEN 200 AND NULL AND name = 'zhangsan'
	 *
	 * @param function
	 * @param value1
	 * @param value2
	 * @return
	 */
	public ZRWrapper<T> notBetween(final SerializableFunction<T, Object> function, final Object value1,
			final Object value2) {
		if ((value1 == null) && (value2 == null)) {
			return this.addValue0(function, null, SQLOperatorEnum.BETWEEN);
		}

		return this.addValue0(function, new Object[] { value1, value2 }, SQLOperatorEnum.NOT_BETWEEN);
	}

	// ----------- 以下方法都是相同语句，即生成[LIMIT]部分：LIMIT M OFFSET N
	// 调用这些方法中的多个，则以最后调用的为准，即按最后调用的来生成 [LIMIT]部分

	/**
	 * 构造 LIMIT 条件，多次调用本方法，会以最后一次为准，每次调用本方法，
	 * 如果已经有了 LIMIT 条件，则本次的覆盖掉上次的
	 *
	 * @param limit
	 * @param offset
	 * @return
	 */
	public synchronized ZRWrapper<T> limit(final Integer limit, final Integer offset) {
		if (!this.limit.isEmpty()) {
			this.limit.clear();
		}

		this.limit.add(MethodRegex.LIMIT + Sort.SPACE + limit + Sort.SPACE + MethodRegex.OFFSET + Sort.SPACE + offset);
		return this;
	}

	/**
	 * 取前N条，等同于limit方法：limit(rows, 0);
	 * 本方法和	limit(final Integer limit, final Integer offset) 会互相覆盖，
	 * 即后调用的值会覆盖掉先调用的值
	 *
	 *
	 * @param n
	 * @return
	 */
	public synchronized ZRWrapper<T> fetchFirstNRows(final Integer n) {
		return this.limit(n, ZERO);
	}

	/**
	 * 只取符合条件的第一条，只取1条
	 *
	 * @return
	 */
	public synchronized ZRWrapper<T> fetchFirst() {
		return this.limit(ONE, ZERO);
	}

	/**
	 * 分页，本方法是不查询总条数的分页，只需要展示每页数据的需求，可以使用本方法。
	 * 如需总数(select count(*) from TABLE)的分页，需要展示[总条数]的分页请使用 ZRepository.page 方法
	 *
	 * 本方法等同于 limit(final Integer limit, final Integer offset)
	 *
	 * 如：
	 * 		fetchPage(1,20)	: 每页显示20条，取第1页
	 * 等同于
	 * 		limit(20,0)		: 取前20条
	 * 生成的SQL都一样：
	 * 		LIMIT 20 OFFSET 0
	 *
	 * 本方法和
	 * 		limit(final Integer limit, final Integer offset)
	 * 		fetchFirstNRows(final Integer n)
	 * 		fetchFirst()
	 * 	四者都会互相覆盖
	 *
	 * @param page 页数，表示当前要取第几页，从1开始
	 * @param size 每页显示的条数，不能小于0
	 * @return
	 */
	public ZRWrapper<T> fetchPage(final Integer page, final Integer size) {
		if (page == null) {
			throw new ZRepositoryException("page 不能为空");
		}

		if (size == null) {
			throw new ZRepositoryException("size 不能为空");
		}

		if (page.intValue() <= 0) {
			throw new ZRepositoryException("page 不能小于1");
		}

		if (size.intValue() <= 0) {
			throw new ZRepositoryException("size 不能小于1");
		}

		final int offset = size * (page - 1);
		final int limit = size;

		return this.limit(limit, offset);
	}

	/**
	 * 	语义同方法：
	 * 		fetchPage(final Integer page, final Integer size)
	 *		fetchFirstNRows(final Integer n)
	 *
	 * 	区别是，本方法是只取第一页，本方法写死为只取第一页。
	 * 	如果只需要第一页数据(只取符合条件的前N条)，使用本方法即可，
	 * 	或者使用 fetchFirstNRows(final Integer n) 都是相同语义
	 *
	 * @param size
	 * @return
	 */
	public ZRWrapper<T> fetchFistPage(final Integer size) {
		return this.fetchPage(ONE, size);
	}

	// ----------- 以上方法都是相同语义，即生成[LIMIT]部分：LIMIT M OFFSET N
	// 调用这些方法中的多个，则以最后调用的为准，即按最后调用的来生成 [LIMIT]部分

	/**
	 * 添加一个升序排序条件
	 *
	 * @param function
	 * @return
	 */
	public ZRWrapper<T> orderByAsc(final SerializableFunction<T, Object> function) {
		return this.orderBy(function, Sort.ASC);
	}

	/**
	 * 添加一个降序排序条件
	 *
	 * @param function
	 * @return
	 */
	public ZRWrapper<T> orderByDesc(final SerializableFunction<T, Object> function) {
		return this.orderBy(function, Sort.DESC);
	}

	private ZRWrapper<T> orderBy(final SerializableFunction<T, Object> function, final String order) {
		if (this.orderBy.size() > 1) {
			this.orderBy.add(ZRepositoryMain.DELIMITER);
		}

		final Field f = ReflectionUtil.getField(function);
		this.orderBy.add(Sort.SPACE + ZFieldConverter.toDbField(f.getName()) + Sort.SPACE + order);
		return this;
	}

	/**
	 * 由本条件，组合另一个条件，两个条件之间的关系为 AND.
	 * 如：
		  	final ZRWrapper<BlobEntity> eq = ZRWrapper.wrap(BlobEntity.class)<>();
			eq.startingWith(BlobEntity::getName,s1.getName());
			eq.eq(BlobEntity::getId,s1.getId());

			final ZRWrapper<BlobEntity> and = ZRWrapper.wrap(BlobEntity.class)<>();
			and.like(BlobEntity::getChar1,s2.getChar1());
			and.gte(BlobEntity::getD1,s2.getD1());

			eq.and(and);

			final List<BlobEntity> find = myRepository.find(eq);

		两个之间之间AND，等同于两个条件的操作全放在一个条件里，如上代码
		等同于：
			final ZRWrapper<BlobEntity> eq = ZRWrapper.wrap(BlobEntity.class)<>();
			eq.startingWith(BlobEntity::getName,s1.getName());
			eq.eq(BlobEntity::getId,s1.getId());
			eq.like(BlobEntity::getChar1,s2.getChar1());
			eq.gte(BlobEntity::getD1,s2.getD1());

			final List<BlobEntity> find = myRepository.find(eq);

		这两段代码生成的SQL相同：
			WHERE name LIKE '?%' AND id = ? AND char1 LIKE '%?%' AND d1 >= ?

	 *
	 * @param wrapper	要组合的条件
	 * @return
	 */
	public ZRWrapper<T> and(final ZRWrapper<T> wrapper) {

		this.where.add(0, "(");
		this.where.add(")");
		this.where.add(AND);

		final String x = wrapper.toString();
		this.where.add(x);

		return this;
	}

	/**
	 * 由本条件，组合另一个条件，两个条件之间的关系为 OR.
	 * 如：

		final ZRWrapper<BlobEntity> eq = ZRWrapper.wrap(BlobEntity.class)<>();
		eq.eq(BlobEntity::getInteger1, s1.getInteger1());
		eq.like(BlobEntity::getName, s1.getName());
		eq.between(BlobEntity::getShort1, s1.getShort1(), s2.getShort1());

		final ZRWrapper<BlobEntity> or = ZRWrapper.wrap(BlobEntity.class)<>();
		or.notNull(BlobEntity::getTime);
		or.lte(BlobEntity::getId,Integer.MAX_VALUE);

		eq.or(or);

		final List<BlobEntity> find = this.rrrrrrr.find(eq);

		这段代码生成的SQL条件为：
			WHERE
			((
					integer1 = ?
					AND NAME LIKE '%?%'
					AND short1 BETWEEN ? AND ?
					)
				OR ((
						time IS NOT NULL
					AND id <= 2147483647
			)))

	 *
	 * @param wrapper	要组合的条件
	 * @return
	 */
	public ZRWrapper<T> or(final ZRWrapper<T> wrapper) {

		this.where.add(0, "(");
		this.where.add(")");
		this.where.add(OR);

		final String x = wrapper.toString();
		this.where.add(x);

		return this;
	}

	@Override
	public String toString() {
		final String x = this.done().getFullWhere();
		return x;
	}

	/**
	 * 按当前的条件，组合出WHERE条件
	 */
	public WR done() {

		final String wH = this.where.isEmpty() ? ALWAYS_TRUE
				: "(" + this.where.stream().collect(Collectors.joining(SPACE)) + ")";

		final String oH = this.orderBy.size() == 1 ? wH : wH + this.orderBy.stream().collect(Collectors.joining(SPACE)) + Sort.SPACE;

		final String lH = this.limit.isEmpty() ? oH : oH + this.limit.stream().collect(Collectors.joining(SPACE));
		final WR wr = new WR();

		wr.setFullWhere(lH);

		wr.setWhere(this.where.isEmpty() ? ALWAYS_TRUE : "(" + this.where.stream().collect(Collectors.joining(SPACE)) + ")" );
		wr.setOrderBy(this.orderBy.size()==1 ? null : this.orderBy.stream().collect(Collectors.joining(SPACE)));
		wr.setLimit(this.limit.isEmpty() ? null : this.limit.stream().collect(Collectors.joining(SPACE)));


		final String sql =
				"SELECT "
						+ SU.gSelectFromReturnType(this.entityClass, this.entityClass)
						+ " FROM "
						+ this.entityClass.getAnnotation(ZEntity.class).tableName()
						+ " WHERE "
						+ wr.getFullWhere()
						;

		wr.setSql(sql);

		return wr;
	}

	private ZRWrapper<T> addValue0(final SerializableFunction<T, Object> function, final Object value,
			final SQLOperatorEnum sqlOperatorEnum) {

		if ((value == null) && ((sqlOperatorEnum != SQLOperatorEnum.IS_NULL) && (sqlOperatorEnum != SQLOperatorEnum.NOT_NULL))) {
			return this;
		}

		final Field f = ReflectionUtil.getField(function);

		final String columnName = ZFieldConverter.toDbField(f.getName());
		if (this.where.isEmpty() || ((this.where.size() == 1) && "(".equals(this.where.get(0).trim()))) {
			this.where.add(columnName + SPACE + sqlOperatorEnum.getContent() + (SPACE + sqlOperatorEnum.hValue(f, value, this.getDBEnum())));
		} else {
			this.where.add(AND + SPACE + columnName + SPACE + sqlOperatorEnum.getContent() + (SPACE + sqlOperatorEnum.hValue(f, value, this.getDBEnum())));
		}

		return this;
	}

	private DBEnum getDBEnum() {
		final String key = this.entityClass.getName();
		return ZRC.singleton().computeIfAbsent(key, this::getDBEnum0);
	}

	private DBEnum getDBEnum0() {
		final String dataSourceName = this.entityClass.getAnnotation(ZEntity.class).dataSourceName();
		final ZCPool pool = ZCPool.getInstance(dataSourceName);
		return pool.getDbEnum(Mode.WRITE);
	}

	public List<String> getLimit() {
		return this.limit;
	}

	public Class<T> getEntityClass() {
		return this.entityClass;
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.entityClass, this.limit, this.orderBy, this.where);
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (this.getClass() != obj.getClass()) {
			return false;
		}
		final ZRWrapper other = (ZRWrapper) obj;
		return Objects.equals(this.entityClass, other.entityClass) && Objects.equals(this.limit, other.limit)
				&& Objects.equals(this.orderBy, other.orderBy) && Objects.equals(this.where, other.where);
	}

}
