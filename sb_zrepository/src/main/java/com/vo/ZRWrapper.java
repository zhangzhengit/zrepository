package com.vo;

import java.lang.reflect.Field;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;

/**
 * 造查询条件,使用方法引用来构造查询条件.
 * 同一个本类对象调用除了and和or以外的方法，默认是AND关系，如：
 *
 * 		final ZRWrapper<BlobEntity> eq = new ZRWrapper<>();
 *		eq.startingWith(BlobEntity::getName, "zhangsan");
 *		eq.isNull(BlobEntity::getChar1);
 *		eq.lt(BlobEntity::getCreateTime,new Date());
 *
 * 生成条件为：
 * 		WHERE name like '?%' AND char1 IS NULL AND create_time < ?
 * eq.后面加再多条件，也同为AND关系，可以不用and方法组合本就是AND关系的多个条件.
 *
 * 如需OR关系，需要再多构造一个OR的本类对象：
 *
 * 		final ZRWrapper<BlobEntity> eq = new ZRWrapper<>();
 *		eq.startingWith(BlobEntity::getName, "zhangsan");
 *		eq.isNull(BlobEntity::getChar1);
 *		eq.lt(BlobEntity::getCreateTime,new Date());
 *
 *  	final ZRWrapper<BlobEntity> or = new ZRWrapper<>();
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
 * 		final ZRWrapper<BlobEntity> eq = new ZRWrapper<>();
 *		eq.eq(BlobEntity::getName, "zhangsan");
 *
 *		生成WHERE：WHERE name = '?'
 *
 * 查询：name = zhangsan 并且 id <= 200
 *		final ZRWrapper<BlobEntity> eq = new ZRWrapper<>();
 *		eq.eq(BlobEntity::getName, "zhangsan");
 *		eq.lte(BlobEntity::getId,200);
 *
 *		生成WHERE：WHERE name = '?' AND id <= ?
 *
 * 查询：name 以张三开头 并且  char1 为空 并且 createTime 小于某个时间点
 * 		final ZRWrapper<BlobEntity> eq = new ZRWrapper<>();
 *		eq.startingWith(BlobEntity::getName, "zhangsan");
 *		eq.isNull(BlobEntity::getChar1);
 *		eq.lt(BlobEntity::getCreateTime,new Date());
 *
 *		生成WHERE：WHERE name like '?%' AND char IS NULL AND create_time < ?
 *
 * 多查询条件（对个本类对象组合出的查询条件）：
 *
 * 查询： 条件1：name 不为空 并且 id 在某个范围内 并且 time 小于某个时间点
 * 	 或者 条件2：byte1 在为空 并且 long1 不等于 3
 *		final ZRWrapper<BlobEntity> eq = new ZRWrapper<>();
		eq.notNull(BlobEntity::getName);
		eq.between(BlobEntity::getId, 200, 5000);
		eq.lt(BlobEntity::getTime, new Date());

		final ZRWrapper<BlobEntity> or = new ZRWrapper<>();
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

  		final ZRWrapper<BlobEntity> eq = new ZRWrapper<>();
		eq.notNull(BlobEntity::getName);
		eq.between(BlobEntity::getId, 200, 5000);
		final List<BlobEntity> find = this.blobRepository.find(eq);

 * @author zhangzhen
 * @date 2024年6月12日 下午7:32:13
 *
 */
public class ZRWrapper<T> {

	private static final String TRUE = " 1 = 1 ";
	public static final String SPACE = " ";
	private static final String AND = MethodRegex.AND;
	private static final String OR = MethodRegex.OR;

	private final List<String> where = Lists.newArrayList();

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
	public ZRWrapper<T> eq(final SerializableFunction<T, Object> function, final Object value) {
		return this.addValue0(function, value, SQLOperatorEnum.EQ);
	}

	private ZRWrapper<T> addValue0(final SerializableFunction<T, Object> function, final Object value, final SQLOperatorEnum sqlOperatorEnum) {
		final Field f = ReflectionUtil.getField(function);

		final String columnName = ZFieldConverter.toDbField(f.getName());
		if (this.where.isEmpty() || ((this.where.size() == 1) && "(".equals(this.where.get(0).trim()))) {
			this.where.add(columnName + SPACE + sqlOperatorEnum.getContent() + (SPACE + sqlOperatorEnum.hValue(value)));
		} else {
			this.where.add(AND + SPACE + columnName + SPACE + sqlOperatorEnum.getContent() + (SPACE + sqlOperatorEnum.hValue(value)));
		}

		return this;
	}

	public ZRWrapper<T> eq(final List<WrapperPair<T>> pairList) {
		if (pairList.size() > 0) {
			this.where.add("(");
			for (final WrapperPair<T> element : pairList) {
				this.eq(element);
			}
			this.where.add(")");
		}
		return this;
	}

	public ZRWrapper<T> eq(final WrapperPair<T> pair) {
		return this.eq(pair.getFunction(), pair.getValue());
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
	public ZRWrapper<T> ne(final SerializableFunction<T, Object> function, final Object value) {
		return this.addValue0(function, value, SQLOperatorEnum.NE);
	}

	public ZRWrapper<T> ne(final List<WrapperPair<T>> pairList) {
		if (pairList.size() > 0) {
			this.where.add("(");
			for (final WrapperPair<T> element : pairList) {
				this.ne(element);
			}
			this.where.add(")");
		}
		return this;
	}

	public ZRWrapper<T> ne(final WrapperPair<T> pair) {
		return this.ne(pair.getFunction(), pair.getValue());
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
	public ZRWrapper<T> lt(final SerializableFunction<T, Object> function, final Object value) {
		return this.addValue0(function, value, SQLOperatorEnum.LT);
	}

	public ZRWrapper<T> lt(final List<WrapperPair<T>> pairList) {
		if (pairList.size() > 0) {
			this.where.add("(");
			for (final WrapperPair<T> element : pairList) {
				this.lt(element);
			}
			this.where.add(")");
		}
		return this;
	}

	public ZRWrapper<T> lt(final WrapperPair<T> pair) {
		return this.lt(pair.getFunction(), pair.getValue());
	}

	/**
	 * 小于等于,构造条件如: id <= ?
	 * 调用本方法的方式为：
	 * 		wrapper.lte(MyEntity::getId(),myEntity.getId());
	 * 或者
	 * 		wrapper.lte(MyEntity::getId(),200);
	 *
	 * @param function
	 * @param value
	 * @return
	 */
	public ZRWrapper<T> lte(final SerializableFunction<T, Object> function, final Object value) {
		return this.addValue0(function, value, SQLOperatorEnum.LTE);
	}

	public ZRWrapper<T> lte(final List<WrapperPair<T>> pairList) {
		if (pairList.size() > 0) {
			this.where.add("(");
			for (final WrapperPair<T> element : pairList) {
				this.lte(element);
			}
			this.where.add(")");
		}
		return this;
	}

	public ZRWrapper<T> lte(final WrapperPair<T> pair) {
		return this.lte(pair.getFunction(), pair.getValue());
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
	public ZRWrapper<T> gt(final SerializableFunction<T, Object> function, final Object value) {
		return this.addValue0(function, value, SQLOperatorEnum.GT);
	}

	public ZRWrapper<T> gt(final List<WrapperPair<T>> pairList) {
		if (pairList.size() > 0) {
			this.where.add("(");
			for (final WrapperPair<T> element : pairList) {
				this.gt(element);
			}
			this.where.add(")");
		}
		return this;
	}

	public ZRWrapper<T> gt(final WrapperPair<T> pair) {
		return this.gt(pair.getFunction(), pair.getValue());
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
	public ZRWrapper<T> gte(final SerializableFunction<T, Object> function, final Object value) {
		return this.addValue0(function, value, SQLOperatorEnum.GTE);
	}

	public ZRWrapper<T> gte(final List<WrapperPair<T>> pairList) {
		if (pairList.size() > 0) {
			this.where.add("(");
			for (final WrapperPair<T> element : pairList) {
				this.gte(element);
			}
			this.where.add(")");
		}
		return this;
	}

	public ZRWrapper<T> gte(final WrapperPair<T> pair) {
		return this.gte(pair.getFunction(), pair.getValue());
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
	public ZRWrapper<T> like(final SerializableFunction<T, Object> function, final String string) {
		return this.addValue0(function, string, SQLOperatorEnum.LIKE);
	}

	public ZRWrapper<T> like(final SerializableFunction<T, Object> function, final Character character) {
		return this.addValue0(function, character, SQLOperatorEnum.LIKE);
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
	 * @param string
	 * @return
	 */
	public ZRWrapper<T> notLike(final SerializableFunction<T, Object> function, final String string) {
		return this.addValue0(function, string, SQLOperatorEnum.NOT_LIKE);
	}

	public ZRWrapper<T> notLike(final SerializableFunction<T, Object> function, final Character character) {
		return this.addValue0(function, character, SQLOperatorEnum.NOT_LIKE);
	}

	// FIXME 2024年6月12日 下午11:19:05 zhangzhen : isNUll和notNull继续测
	public ZRWrapper<T> isNull(final WrapperPair<T> pair) {
		return this.isNull(pair.getFunction());
	}

	/**
	 * 判断column is null,构造条件如: name IS NUll
	 *
	 * 调用本方法的方式为：
	 * 		wrapper.isNull(MyEntity::getName());
	 * 或者
	 * 		wrapper.isNull(MyEntity::getName());
	 *
	 * @param function
	 * @param value
	 * @return
	 */
	public ZRWrapper<T> isNull(final SerializableFunction<T, Object> function) {
		return this.addValue0(function, null, SQLOperatorEnum.IS_NULL);
	}

	public ZRWrapper<T> notNull(final WrapperPair<T> pair) {
		return this.notNull(pair.getFunction());
	}

	/**
	 * 判断column IS NOT NULL,构造条件如: name IS NOT NUll
	 *
	 * 调用本方法的方式为：
	 * 		wrapper.notNull(MyEntity::getName());
	 * 或者
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
	 * 后缀匹配查询,构造条件如: name LIKE '?%'
	 *
	 * 调用本方法的方式为：
	 * 		wrapper.endingWith(MyEntity::getName(),myEntity.getName());
	 * 或者
	 * 		wrapper.endingWith(MyEntity::getName(),"张三李四");
	 *
	 * @param function
	 * @param string
	 * @return
	 */
	// FIXME 2024年6月13日 下午11:21:34 zhangzhen : lt ge 等等所有方法都加入重载方法
	public ZRWrapper<T> endingWith(final SerializableFunction<T, Object> function, final String string) {
		return this.addValue0(function, string, SQLOperatorEnum.ENDING_WITH);
	}

	public ZRWrapper<T> endingWith(final SerializableFunction<T, Object> function, final Character character) {
		return this.addValue0(function, character, SQLOperatorEnum.ENDING_WITH);
	}

	/**
	 * 前缀匹配查询,构造条件如: name LIKE '%?'
	 *
	 * 调用本方法的方式为：
	 * 		wrapper.startingWith(MyEntity::getName(),myEntity.getName());
	 * 或者
	 * 		wrapper.startingWith(MyEntity::getName(),"张三李四");
	 *
	 * @param function
	 * @param string
	 * @return
	 */
	public ZRWrapper<T> startingWith(final SerializableFunction<T, Object> function, final String string) {
		return this.addValue0(function, string, SQLOperatorEnum.STARTING_WITH);
	}

	public ZRWrapper<T> startingWith(final SerializableFunction<T, Object> function, final Character character) {
		return this.addValue0(function, character, SQLOperatorEnum.STARTING_WITH);
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
	 * @param iterable	传值多个条件值，类型为Iterable
	 * @return
	 */
	public ZRWrapper<T> in(final SerializableFunction<T, Object> function, final Iterable<?> iterable) {
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
	 * @param iterable	传值多个条件值，类型为Iterable
	 * @return
	 */
	public ZRWrapper<T> notIn(final SerializableFunction<T, Object> function, final Iterable<?> iterable) {
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
	 * @param function
	 * @param value1
	 * @param value2
	 * @return
	 */
	public ZRWrapper<T> between(final SerializableFunction<T, Object> function, final Object value1, final Object value2) {
		return this.addValue0(function, new Object[] { value1, value2 }, SQLOperatorEnum.BETWEEN);
	}

	/**
	 * NOT BETWEEN 范围查询,构造条件如: name NOT BETWEEN ? AND ?
	 *
	 * 调用本方法的方式为：
	 * 		wrapper.notBetween(MyEntity::getId,e1.getId(),e2.getId());
	 * 或者
	 * 		wrapper.notBetween(MyEntity::getId,1,2);
	 *
	 * @param function
	 * @param value1
	 * @param value2
	 * @return
	 */
	public ZRWrapper<T> notBetween(final SerializableFunction<T, Object> function, final Object value1, final Object value2) {
		return this.addValue0(function, new Object[] { value1, value2 }, SQLOperatorEnum.NOT_BETWEEN);
	}

	// FIXME 2024年6月13日 下午8:55:56 zhangzhen : 继续支持

	/**
	 * 由本条件，组合另一个条件，两个条件之间的关系为 AND.
	 * 如：
		  	final ZRWrapper<BlobEntity> eq = new ZRWrapper<>();
			eq.startingWith(BlobEntity::getName,s1.getName());
			eq.eq(BlobEntity::getId,s1.getId());

			final ZRWrapper<BlobEntity> and = new ZRWrapper<>();
			and.like(BlobEntity::getChar1,s2.getChar1());
			and.gte(BlobEntity::getD1,s2.getD1());

			eq.and(and);

			final List<BlobEntity> find = myRepository.find(eq);

		两个之间之间AND，等同于两个条件的操作全放在一个条件里，如上代码
		等同于：
			final ZRWrapper<BlobEntity> eq = new ZRWrapper<>();
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

		final ZRWrapper<BlobEntity> eq = new ZRWrapper<>();
		eq.eq(BlobEntity::getInteger1, s1.getInteger1());
		eq.like(BlobEntity::getName, s1.getName());
		eq.between(BlobEntity::getShort1, s1.getShort1(), s2.getShort1());

		final ZRWrapper<BlobEntity> or = new ZRWrapper<>();
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
		final String x = this.done();
		return "(" + x + ")";
	}

	/**
	 * 按当前的条件，组合出WHERE条件
	 */
	public String done() {
		final String w =
				this.where.isEmpty()
				? TRUE : "(" + this.where.stream().collect(Collectors.joining(SPACE)) + ")";
		return w;
	}

}
