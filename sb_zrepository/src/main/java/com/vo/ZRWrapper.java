package com.vo;

import java.lang.reflect.Field;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
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

	@Getter
	List<String> where = Lists.newArrayList();

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
		this.eq(pair.getFunction(), pair.getValue());
		return this;
	}

	public ZRWrapper<T> eq(final SerializableFunction<T, Object> function, final Object value) {

		final Field f = ReflectionUtil.getField(function);
		if (this.where.isEmpty() || ((this.where.size() == 1) && "(".equals(this.where.get(0).trim()))) {
			this.where.add(f.getName() + "=" + hValue(value));
		} else {
			this.where.add("and" + " " + f.getName() + "=" + hValue(value));
		}

		return this;
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

	public ZRWrapper<T> ne(final List<WrapperPair<T>> pairList) {
		if (pairList.size() > 0) {
			if (this.where.size() > 1) {
				this.where.add("and");
			}
			this.where.add("(");
			for (int i = 0; i < pairList.size(); i++) {

				this.ne(pairList.get(i));
				if (i < (pairList.size() - 1)) {
					this.where.add("and");
				}
			}
			this.where.add(")");
		}
		return this;
	}

	public ZRWrapper<T> ne(final WrapperPair<T> pair) {
		this.ne(pair.getFunction(), pair.getValue());
		return this;
	}

	public ZRWrapper<T> ne(final SerializableFunction<T, Object> function, final Object value) {

		final Field f = ReflectionUtil.getField(function);
		// if (this.where.isEmpty() || ((this.where.size() == 1) &&
		// "(".equals(this.where.get(0).trim()))) {
		this.where.add(f.getName() + "!=" + hValue(value));
		// } else {
		// this.where.add("and" + " " + f.getName() + "!=" + value);
		// }

		return this;
	}

	// and
	public ZRWrapper<T> and(final ZRWrapper<T> wrapper) {

		final List<String> w = wrapper.getWhere();
		if (!w.isEmpty()) {
			this.where.add("and");
			for (final String w1 : w) {
				this.where.add(w1);
			}
		}

		return this;
	}

	public ZRWrapper<T> or(final ZRWrapper<T> wrapper) {

		final String x = wrapper.toString();

		this.where.add(0, "(");
		this.where.add(")");
		this.where.add("or");
		this.where.add(x);

		return this;
	}

	@Override
	public String toString() {
		// return super.toString();

		final String x = this.done();

		return "(" + x + ")";
	}

	/**
	 * 完成，组装SQL了
	 */
	public String done() {
		final String w = this.where.isEmpty() ? "" : "(" + this.where.stream().collect(Collectors.joining(" ")) + ")";

		System.out.println("done");
		System.out.println("where = " + this.where);
		System.out.println("w = " + w);

		return w;
	}

}
