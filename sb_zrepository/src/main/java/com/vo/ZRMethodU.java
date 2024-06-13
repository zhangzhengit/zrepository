package com.vo;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Date;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

/**
 *
 *
 * @author zhangzhen
 * @date 2024年6月13日 下午10:15:23
 *
 */
public class ZRMethodU {

	/**
	 * < / <= / > / >= 支持的字段类型
	 */
	public static final ImmutableSet<Class<?>> GTE = ImmutableSet.copyOf(Sets.newHashSet(
			Date.class,java.sql.Date.class,Time.class,Timestamp.class,
			Byte.class, Short.class, Integer.class, Long.class,
			Float.class, Double.class, BigDecimal.class, BigInteger.class, String.class, Character.class));

	/**
	 * LIKE / NOT LIKE 支持的字段类型
	 */
	public static final ImmutableSet<Class<?>> STARTING_WITH = ImmutableSet.copyOf(Sets.newHashSet(String.class, Character.class));

	public static boolean like(final Class<?> fieldClass) {
		return endingWith(fieldClass);

	}
	public static boolean startingWith(final Class<?> fieldClass) {
		return STARTING_WITH.contains(fieldClass);
	}

	public static boolean endingWith(final Class<?> fieldClass) {
		return startingWith(fieldClass);
	}

	public static boolean gte(final Class<?> fieldClass) {
		final boolean contains = GTE.contains(fieldClass);
		return contains;
	}

}
