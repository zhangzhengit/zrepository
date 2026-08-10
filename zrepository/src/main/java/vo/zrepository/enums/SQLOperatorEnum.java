package vo.zrepository.enums;

import java.lang.reflect.Field;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashSet;
import java.util.StringJoiner;

import org.apache.commons.codec.binary.Hex;

import com.google.common.collect.Sets;

import vo.zrepository.anno.ZDateFormat;
import vo.zrepository.anno.ZDateFormatEnum;
import vo.zrepository.core.MethodRegex;
import vo.zrepository.core.ZRMethodU;
import vo.zrepository.core.ZRWrapper;
import vo.zrepository.exception.ZRWrapperTypeException;

/**
 *
 * SQL运算符
 *
 * @author zhangzhen
 * @date 2024年6月12日 下午10:30:29
 *
 */
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
				throw new UnsupportedOperationException("LT操作:参数类型" + value.getClass().getName() + "不支持");
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
				throw new UnsupportedOperationException("LTE操作:参数类型" + value.getClass().getName() + "不支持");
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
				throw new UnsupportedOperationException("GT操作:参数类型" + value.getClass().getName() + "不支持");
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
				throw new UnsupportedOperationException("GTE操作:参数类型" + value.getClass().getName() + "不支持");
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
				throw new UnsupportedOperationException("LIKE操作:参数类型" + value.getClass().getName() + "不支持");
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
				throw new UnsupportedOperationException("NOT_LIKE操作:参数类型" + value.getClass().getName() + "不支持");
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
				throw new UnsupportedOperationException("ENDING_WITH操作:参数类型" + value.getClass().getName() + "不支持");
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
				throw new UnsupportedOperationException("STARTING_WITH操作:参数类型" + value.getClass().getName() + "不支持");
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

		switch (dbEnum) {
		case SQLITE:
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
			if ((value instanceof java.sql.Date) || (value instanceof Time)
					|| (value instanceof Timestamp)) {
				return "'" + value + "'";
			}
			if ((value instanceof LocalDate)) {
				final LocalDate localDate = (LocalDate) value;
				final LocalDateTime localDateTime = localDate.atStartOfDay();
				final long epochMilli = localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
				return "'" + epochMilli + "'";
			}
			if ((value instanceof LocalTime)) {
				final LocalTime localTime = (LocalTime) value;
				return "'" + localTime + "'";
			}
			if ((value instanceof LocalDateTime)) {
				final LocalDateTime localDateTime = (LocalDateTime) value;
				final long epochMilli = localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
				return "'" + epochMilli + "'";
			}
			break;
		case MYSQL:
			if (value instanceof Date) {
				final ZDateFormat zdf = field.getAnnotation(ZDateFormat.class);
				if (zdf != null) {
					final ZDateFormatEnum format = zdf.format();
					final SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format.getFormat());
					final String v = simpleDateFormat.format((Date) value);
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
			break;
		case POSTGRESQL:
			if ((value instanceof Date) || (value instanceof java.sql.Date) || (value instanceof Time)
					|| (value instanceof Timestamp)
					|| (value instanceof LocalDate)
					|| (value instanceof LocalDateTime)
					|| (value instanceof LocalTime)
					) {
				return "'" + value + "'";
			}
			break;
		default:
			break;
		}

		return value;
	}

	private static Object checkArray(final Object value) {

		if (value.getClass().isArray()) {
			final String encodeHexStr = Hex.encodeHexString((byte[]) value);
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
			final String m = "参数必须是" + Iterable.class.getSimpleName() + "类型,当前类型为[" + value.getClass().getName()
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

	SQLOperatorEnum(final String content, final String remark) {
		this.content = content;
		this.remark = remark;
	}

	public String getContent() {
		return this.content;
	}

	public void setContent(final String content) {
		this.content = content;
	}

	public String getRemark() {
		return this.remark;
	}

	public void setRemark(final String remark) {
		this.remark = remark;
	}

}
