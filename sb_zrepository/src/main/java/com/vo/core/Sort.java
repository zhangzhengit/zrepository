package com.vo.core;

import java.lang.reflect.Field;
import java.util.List;

import com.google.common.collect.Lists;
import com.vo.ReflectionUtil;
import com.vo.SerializableFunction;

/**
 * ZRepository.page 方法的排序条件对象
 *
 * @author zhangzhen
 * @data 2024年5月19日 下午4:40:49
 *
 */
// FIXME 2024年6月11日 下午10:30:50 zhangzhen : 继续写：用方法引用随意构造>= < = like between等等条件的类
public class Sort<T> {

	public static final String SPACE = " ";

	private static final String ASC = " asc";

	private static final String DESC = " desc";

	private static final String ORDER_BY = "order by";

	private final List<String> x = Lists.newArrayList();

	public static <T> Sort<T> empty() {
		return new Sort<>();
	}

	public Sort<T> ascendingBy(final String column) {
		this.addOrderBy();
		this.x.add(SPACE + column + ASC);
		return this;
	}

	public Sort<T> ascendingBy(final SerializableFunction<T, Object> function) {
		final Field field = ReflectionUtil.getField(function);
		this.addOrderBy();
		final String name = field.getName();
		this.x.add(SPACE + name + ASC);
		return this;
	}

	public Sort<T> descendingBy(final SerializableFunction<T, Object> function) {
		final Field field = ReflectionUtil.getField(function);

		this.addOrderBy();
		final String name = field.getName();
		this.x.add(SPACE + name + DESC);
		return this;
	}

	public Sort<T> descendingBy(final String column) {
		this.addOrderBy();
		this.x.add(SPACE + column + DESC);
		return this;
	}

	public String done() {
		final StringBuilder builder = new StringBuilder();
		for (int i = 0; i < this.x.size(); i++) {
			builder.append(this.x.get(i));
			if ((i > 0) && (i < (this.x.size() - 1))) {
				builder.append(',');
			}
		}
		return builder.toString();
	}

	private void addOrderBy() {
		if (this.x.isEmpty()) {
			this.x.add(ORDER_BY);
		}
	}

}
