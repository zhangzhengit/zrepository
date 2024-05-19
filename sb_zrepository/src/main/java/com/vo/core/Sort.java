package com.vo.core;

import java.util.List;

import com.google.common.collect.Lists;

/**
 * ZRepository.page 方法的排序条件对象
 *
 * @author zhangzhen
 * @data 2024年5月19日 下午4:40:49
 *
 */
public class Sort {

	public static final String SPACE = " ";

	private static final String ASC = " asc";

	private static final String DESC = " desc";

	private static final String ORDER_BY = "order by";

	private final List<String> x = Lists.newArrayList();

	public static Sort empty() {
		return create();
	}

	public static Sort create() {
		return new Sort();
	}

	public Sort ascendingBy(final String column) {
		this.addOrderBy();
		this.x.add(SPACE + column + ASC);
		return this;
	}

	public Sort descendingBy(final String column) {
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
