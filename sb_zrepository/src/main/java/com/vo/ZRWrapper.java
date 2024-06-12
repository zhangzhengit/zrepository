package com.vo;

import java.lang.reflect.Field;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.codec.binary.Hex;

import com.google.common.collect.Lists;

import cn.hutool.core.util.HexUtil;
import lombok.Getter;

/**
 * 构造查询条件
 *
 * @param <T>
 *
 * @author zhangzhen
 * @date 2024年6月12日 下午7:32:13
 *
 */
public class ZRWrapper<T> {


	private static final String SPACE = " ";
	private static final String AND = MethodRegex.AND;
	private static final String OR = MethodRegex.OR;

	@Getter
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

		final String fValue = Objects.isNull(value) ? "" : SPACE + hValue(sqlOperatorEnum.hValue(value));

		if (this.where.isEmpty() || ((this.where.size() == 1) && "(".equals(this.where.get(0).trim()))) {
			this.where.add(f.getName() + SPACE + sqlOperatorEnum.getContent() + fValue);
		} else {
			this.where.add(AND + SPACE + f.getName() + SPACE + sqlOperatorEnum.getContent() + fValue);
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

	// FIXME 2024年6月12日 下午9:10:22 zhangzhen : sqlite 中时间日期类型存的是long值
	private static Object hValue(final Object value) {
		if ((value instanceof Character) || (value instanceof String)) {
			return "'" + value + "'";
		}
		if ((value instanceof Date) || (value instanceof java.sql.Date) || (value instanceof Time)
				|| (value instanceof Timestamp)) {
			return "'" + value + "'";
		}

		if (value.getClass().isArray()) {
			final String encodeHexStr = HexUtil.encodeHexStr((byte[]) value);
			final String encodeHexString = Hex.encodeHexString((byte[]) value);
			// final int x = 10;
			// return "'" + encodeHexString + "'";
			// FIXME 2024年6月12日 下午9:08:10 zhangzhen : 还有点问题，暂不支持
			throw new UnsupportedOperationException("array类型暂不支持");
			// FIXME 2024年6月12日 下午9:09:07 zhangzhen : float 等值查询也有问题，干脆在DBType中不支持float算了?
		}

		return value;
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

	public ZRWrapper<T> like(final WrapperPair<T> pair) {
		return this.like(pair.getFunction(), pair.getValue());
	}

	public ZRWrapper<T> like(final SerializableFunction<T, Object> function, final Object value) {
		return this.addValue0(function, value, SQLOperatorEnum.LIKE);
	}

	public ZRWrapper<T> notLike(final WrapperPair<T> pair) {
		return this.like(pair.getFunction(), pair.getValue());
	}

	public ZRWrapper<T> notLike(final SerializableFunction<T, Object> function, final Object value) {
		return this.addValue0(function, value, SQLOperatorEnum.NOT_LIKE);
	}

	// FIXME 2024年6月12日 下午11:19:05 zhangzhen : isNUll和notNull继续测
	public ZRWrapper<T> isNull(final WrapperPair<T> pair) {
		return this.isNull(pair.getFunction());
	}

	public ZRWrapper<T> isNull(final SerializableFunction<T, Object> function) {
		return this.addValue0(function, null, SQLOperatorEnum.IS_NULL);
	}

	public ZRWrapper<T> notNull(final WrapperPair<T> pair) {
		return this.isNull(pair.getFunction());
	}

	public ZRWrapper<T> notNull(final SerializableFunction<T, Object> function) {
		return this.addValue0(function, null, SQLOperatorEnum.NOT_NULL);
	}

	public ZRWrapper<T> endingWith(final WrapperPair<T> pair) {
		return this.endingWith(pair.getFunction(), pair.getValue());
	}

	public ZRWrapper<T> endingWith(final SerializableFunction<T, Object> function, final Object value) {
		return this.addValue0(function, value, SQLOperatorEnum.ENDING_WITH);
	}
	// FIXME 2024年6月12日 下午11:34:01 zhangzhen : 继续支持StartingWith 等，继续做今天留下来的FIXME

	public ZRWrapper<T> gte(final WrapperPair<T> pair) {
		return this.gte(pair.getFunction(), pair.getValue());
	}

	// and
	public ZRWrapper<T> and(final ZRWrapper<T> wrapper) {

		this.where.add(0, "(");
		this.where.add(")");
		this.where.add(AND);

		final String x = wrapper.toString();
		this.where.add(x);

		return this;
	}

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
	 * 完成，组装SQL了
	 */
	public String done() {
		final String w = this.where.isEmpty() ? "" : "(" + this.where.stream().collect(Collectors.joining(SPACE)) + ")";

		System.out.println("done");
		System.out.println("where = " + this.where);
		System.out.println("w = " + w);

		return w;
	}

}
