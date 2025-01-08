package com.vo;

import java.lang.reflect.Field;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
import java.util.HashSet;
import java.util.StringJoiner;

import org.apache.commons.codec.binary.Hex;

import com.google.common.collect.Sets;
import com.vo.exception.ZRWrapperTypeException;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.HexUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 *
 * SQL运算符
 *
 * @author zhangzhen
 * @date 2024年6月12日 下午10:30:29
 *
 */
@Getter
@AllArgsConstructor
public enum SQLOperatorEnum {

	EQ("=", "等于") {
		@Override
		public Object hValue(final Field field, final Object value, final DBEnum dbEnum) {
			if (value == null) {
				return value;
			}

			return addAll(field, value, dbEnum);
		}
	},

	NE("!=", "不等于") {
		@Override
		public Object hValue(final Field field, final Object value, final DBEnum dbEnum) {
			if (value == null) {
				return value;
			}

			return addAll(field, value, dbEnum);
		}
	},

	LT("<", "小于") {
		@Override
		public Object hValue(final Field field, final Object value, final DBEnum dbEnum) {

			if (value == null) {
				return value;
			}

			if (!ZRMethodU.gte(value.getClass())) {
				throw new UnsupportedOperationException("LT操作:参数类型" + value.getClass().getCanonicalName() + "不支持");
			}
			return addAll(field, value, dbEnum);
		}
	},

	LTE("<=", "小于等于") {
		@Override
		public Object hValue(final Field field, final Object value, final DBEnum dbEnum) {
			if (value == null) {
				return value;
			}

			if (!ZRMethodU.gte(value.getClass())) {
				throw new UnsupportedOperationException("LTE操作:参数类型" + value.getClass().getCanonicalName() + "不支持");
			}
			return addAll(field, value, dbEnum);
		}
	},

	GT(">", "大于") {
		@Override
		public Object hValue(final Field field, final Object value, final DBEnum dbEnum) {
			if (value == null) {
				return value;
			}

			if (!ZRMethodU.gte(value.getClass())) {
				throw new UnsupportedOperationException("GT操作:参数类型" + value.getClass().getCanonicalName() + "不支持");
			}
			return addAll(field, value, dbEnum);
		}
	},

	GTE(">=", "大于等于") {
		@Override
		public Object hValue(final Field field, final Object value, final DBEnum dbEnum) {
			if (value == null) {
				return value;
			}

			if (!ZRMethodU.gte(value.getClass())) {
				throw new UnsupportedOperationException("GTE操作:参数类型" + value.getClass().getCanonicalName() + "不支持");
			}
			return addAll(field, value, dbEnum);
		}
	},

	LIKE("LIKE", "模糊查询:是") {
		@Override
		public Object hValue(final Field field, final Object value, final DBEnum dbEnum) {
			// FIXME 2024年6月12日 下午10:52:30 zhangzhen : 这个也要判断类型，
			//			 并且： 还是要提供一个日期时间类型的注解，用在@ZEntity的字段上，在此取此注解的格式来格式化日期时间
			// 否则容易查询此问题： timestamp 类型 在此会生成 %2024-06-12 22:51:12.0% ，而db中值是  2024-06-12 22:51:12
			// 导致like查不出

			if (value == null) {
				return addAll(field, "%" + value + "%", dbEnum);
			}

			if (!ZRMethodU.like(value.getClass())) {
				throw new UnsupportedOperationException("LIKE操作:参数类型" + value.getClass().getCanonicalName() + "不支持");
			}

			return addAll(field, "%" + value + "%", dbEnum);
		}
	},

	NOT_LIKE("NOT LIKE", "模糊查询:非") {
		@Override
		public Object hValue(final Field field, final Object value, final DBEnum dbEnum) {
			if (value == null) {
				return addAll(field, "%" + value + "%", dbEnum);
			}

			if (!ZRMethodU.like(value.getClass())) {
				throw new UnsupportedOperationException("NOT_LIKE操作:参数类型" + value.getClass().getCanonicalName() + "不支持");
			}

			return addAll(field, "%" + value + "%", dbEnum);
		}
	},

	IS_NULL("IS NULL", "判断某个column为null") {
		@Override
		public Object hValue(final Field field, final Object value, final DBEnum dbEnum) {
			// IS_NULL这个和NOT_NULL return "",等同于组装where条件的时候忽略掉此值
			return "";
		}
	},

	NOT_NULL("IS NOT NULL", "判断某个column为not null") {
		@Override
		public Object hValue(final Field field, final Object value, final DBEnum dbEnum) {
			return "";
		}
	},

	ENDING_WITH("LIKE", "模糊查询:匹配后缀") {
		@Override
		public Object hValue(final Field field, final Object value, final DBEnum dbEnum) {
			if (value == null) {
				return addAll(field, "%" + value + "%", dbEnum);
			}

			if (!ZRMethodU.startingWith(value.getClass())) {
				throw new UnsupportedOperationException("ENDING_WITH操作:参数类型" + value.getClass().getCanonicalName() + "不支持");
			}
			return addAll(field, "%" + value, dbEnum);
		}
	},

	STARTING_WITH("LIKE", "模糊查询:匹配前缀") {
		@Override
		public Object hValue(final Field field, final Object value, final DBEnum dbEnum) {
			if (value == null) {
				return addAll(field, "%" + value + "%", dbEnum);
			}

			if (!ZRMethodU.startingWith(value.getClass())) {
				throw new UnsupportedOperationException("STARTING_WITH操作:参数类型" + value.getClass().getCanonicalName() + "不支持");
			}
			return addAll(field, value + "%", dbEnum);
		}
	},

	IN("IN", "IN查询") {
		@Override
		public Object hValue(final Field field, final Object value, final DBEnum dbEnum) {
			return inAndNotIn(field, value, dbEnum);
		}
	},

	NOT_IN("NOT IN", "NOT IN查询") {
		@Override
		public Object hValue(final Field field, final Object value, final DBEnum dbEnum) {
			return inAndNotIn(field, value, dbEnum);
		}
	},

	BETWEEN("BETWEEN", "范围查询:在某个范围内") {
		@Override
		public Object hValue(final Field field, final Object value, final DBEnum dbEnum) {
			return betweenAndNotBetween(field, value, dbEnum);
		}
	},

	NOT_BETWEEN("NOT BETWEEN", "范围查询:不在某个范围内") {
		@Override
		public Object hValue(final Field field, final Object value, final DBEnum dbEnum) {
			return betweenAndNotBetween(field, value, dbEnum);
		}
	},;

	private String content;
	private String remark;

	/**
	 * 根据操作符返回处理后的值，如：对于like符号,返回新值[%value%]
	 * @param field TODO
	 * @param value
	 * @param dbEnum TODO
	 *
	 * @return
	 */
	public abstract Object hValue(Field field, Object value, DBEnum dbEnum);

	private static Object addAll(final Field field, final Object value, final DBEnum dbEnum) {
		final Object v2 = addString(field, value, dbEnum);
		final Object v3 = addDate_Time_Timestamp(field, v2, dbEnum);
		return v3;
	}

	private static Object addDate_Time_Timestamp(final Field field, final Object value, final DBEnum dbEnum) {
		checkArray(value);

		if (dbEnum == DBEnum.SQLITE) {
			// XXX 2024年6月14日 下午8:37:43 zhangzhen : ubunt-sqlite-3.30.1版本(其他版本没测)：
			// time/date/datetime/timestamp 类型存储池long值，所以在此转为long类型
			if (value instanceof java.util.Date) {
				final Date vd = (Date) value;
				final long time = vd.getTime();
				return "'" + time + "'";
			}
			if (value instanceof java.sql.Date) {
				final java.sql.Date vsd = (java.sql.Date) value;
				final long time = vsd.getTime();
				return "'" + time + "'";
			}
			if (value instanceof java.sql.Time) {
				final java.sql.Time vt = (java.sql.Time) value;
				final long time = vt.getTime();
				return "'" + time + "'";
			}
			if (value instanceof java.sql.Timestamp) {
				final java.sql.Timestamp vtt = (java.sql.Timestamp) value;
				final long time = vtt.getTime();
				return "'" + time + "'";
			}
		} else if (dbEnum == DBEnum.MYSQL) {
			if (value instanceof Date) {
				final ZDateFormat zdf = field.getAnnotation(ZDateFormat.class);
				if (zdf != null) {
					final ZDateFormatEnum format = zdf.format();
					final String v = DateUtil.format((Date) value, format.getFormat());
					return "'" + v + "'";
				}
				return "'" + value + "'";
			}
			if ((value instanceof java.sql.Date) || (value instanceof Time)
					|| (value instanceof Timestamp)) {
				return "'" + value + "'";
			}
			if ((value instanceof LocalDate)
					|| (value instanceof LocalTime)
					|| (value instanceof LocalDateTime)
					) {

				return "'" + value + "'";
			}
		} else if (dbEnum == DBEnum.POSTGRESQL) {
			if ((value instanceof Date) || (value instanceof java.sql.Date) || (value instanceof Time)
					|| (value instanceof Timestamp)) {
				return "'" + value + "'";
			}
		}

		return value;
	}

	private static Object checkArray(final Object value) {

		if (value.getClass().isArray()) {
			final String encodeHexStr = HexUtil.encodeHexStr((byte[]) value);
			final String encodeHexString = Hex.encodeHexString((byte[]) value);
			// final int x = 10;
			// return "'" + encodeHexString + "'";
			// FIXME 2024年6月12日 下午9:08:10 zhangzhen : 还有点问题，不支持
			throw new UnsupportedOperationException("array类型不支持");
		}

		return value;
	}

	private static Object addString(final Field field, final Object value, final DBEnum dbEnum) {
		checkArray(value);
		if ((value instanceof Character) || (value instanceof String)) {
			return "'" + value + "'";
		}

		return value;
	}

	private static Object inAndNotIn(final Field field, final Object value, final DBEnum dbEnum) {
		if (value == null) {
			return "(" + value + ")";
		}

		if (!(value instanceof Iterable)) {
			final String m = "参数必须是" + Iterable.class.getSimpleName() + "类型,当前类型为[" + value.getClass().getCanonicalName()
					+ "]";
			throw new ZRWrapperTypeException(m);
		}

		final Iterable<?> i = (Iterable<?>) value;
		final HashSet<Object> set = Sets.newHashSet();
		boolean h = false;
		for (final Object v : i) {
			h = true;
			final Object vR = addAll(field, v, dbEnum);
			set.add(vR);
		}

		if (!h) {
			return "(null)";
		}

		final StringJoiner joiner = new StringJoiner(",", "(", ")");
		for (final Object vR : set) {
			joiner.add(String.valueOf(vR));
		}

		return joiner.toString();
	}

	private static Object betweenAndNotBetween(final Field field, final Object value, final DBEnum dbEnum) {
		if (value == null) {
			return ZRWrapper.NULL + ZRWrapper.SPACE + MethodRegex.AND + ZRWrapper.SPACE + ZRWrapper.NULL;
		}

		final Object[] a = (Object[]) value;
		if (a.length == 0) {
			return ZRWrapper.NULL + ZRWrapper.SPACE + MethodRegex.AND + ZRWrapper.SPACE + ZRWrapper.NULL;
		}

		if (a.length == 1) {
			final Object v1 = addAll(field, a[0], dbEnum);
			return v1 + ZRWrapper.SPACE + MethodRegex.AND + ZRWrapper.SPACE + ZRWrapper.NULL;
		}

		final Object v1 = a[0] == null ? ZRWrapper.NULL : addAll (field, a[0], dbEnum);
		final Object v2 = a[1] == null ? ZRWrapper.NULL : addAll (field, a[1], dbEnum);
		return v1 + ZRWrapper.SPACE + MethodRegex.AND + ZRWrapper.SPACE + v2;
	}
}
