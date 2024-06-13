package com.vo;

import java.sql.Time;
import java.sql.Timestamp;
import java.util.Date;
import java.util.StringJoiner;

import org.apache.commons.codec.binary.Hex;

import com.vo.exception.ZRWrapperTypeException;

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
		public Object hValue(final Object value) {
			return addAll(value);
		}
	},

	NE("!=", "不等于") {
		@Override
		public Object hValue(final Object value) {
			return addAll(value);
		}
	},

	LT("<", "小于") {
		@Override
		public Object hValue(final Object value) {
			return addAll(value);
		}
	},

	LTE("<=", "小于等于") {
		@Override
		public Object hValue(final Object value) {
			return addAll(value);
		}
	},

	GT(">", "大于") {
		@Override
		public Object hValue(final Object value) {
			return addAll(value);
		}
	},

	GTE(">=", "大于等于") {
		@Override
		public Object hValue(final Object value) {
			return addAll(value);
		}
	},

	LIKE("LIKE", "模糊查询:是") {
		@Override
		public Object hValue(final Object value) {
			// FIXME 2024年6月12日 下午10:52:30 zhangzhen : 这个也要判断类型，
			//			 并且： 还是要提供一个日期时间类型的注解，用在@ZEntity的字段上，在此取此注解的格式来格式化日期时间
			// 否则容易查询此问题： timestamp 类型 在此会生成 %2024-06-12 22:51:12.0% ，而db中值是  2024-06-12 22:51:12
			// 导致like查不出
			// FIXME 2024年6月12日 下午10:58:37 zhangzhen : 或者：和声明式方法一样，判断各个方法支持的类型，如：like 只支持String/Character等等
			// FIXME 2024年6月12日 下午11:01:37 zhangzhen : 决定了：改为和checkFindByXXLike一样，先把各个方法支持的类型提取为一个类

			return addAll("%" + value + "%");
		}
	},

	NOT_LIKE("NOT LIKE", "模糊查询:非") {
		@Override
		public Object hValue(final Object value) {
			return addAll("%" + value + "%");
		}
	},

	IS_NULL("IS NULL", "判断某个column为null") {
		@Override
		public Object hValue(final Object value) {
			return value;
		}
	},

	NOT_NULL("IS NOT NULL", "判断某个column为not null") {
		@Override
		public Object hValue(final Object value) {
			return value;
		}
	},

	ENDING_WITH("LIKE", "模糊查询:匹配后缀") {
		@Override
		public Object hValue(final Object value) {
			return addAll("%" + value);
		}
	},

	STARTING_WITH("LIKE", "模糊查询:匹配前缀") {
		@Override
		public Object hValue(final Object value) {
			return addAll(value + "%");
		}
	},

	IN("IN", "IN查询") {
		@Override
		public Object hValue(final Object value) {
			return inAndNotIn(value);
		}
	},

	NOT_IN("NOT IN", "NOT IN查询") {
		@Override
		public Object hValue(final Object value) {
			return inAndNotIn(value);
		}
	},

	BETWEEN("BETWEEN", "范围查询:在某个范围内") {
		@Override
		public Object hValue(final Object value) {
			final Object[] a = (Object[]) value;
			final Object v1 = addAll(a[0]);
			final Object v2 = addAll(a[1]);
			return v1 + ZRWrapper.SPACE + MethodRegex.AND  + ZRWrapper.SPACE+ v2;
		}
	},

	NOT_BETWEEN("NOT BETWEEN", "范围查询:不在某个范围内") {
		@Override
		public Object hValue(final Object value) {
			final Object[] a = (Object[]) value;
			final Object v1 = addAll(a[0]);
			final Object v2 = addAll(a[1]);
			return v1 + ZRWrapper.SPACE + MethodRegex.AND + ZRWrapper.SPACE + v2;
		}
	},

	// FIXME 2024年6月12日 下午11:30:09 zhangzhen : 继续支持操作，MethodRegex中的都实现出来
	;

	private String content;
	private String remark;

	/**
	 * 根据操作符返回处理后的值，如：对于like符号,返回新值[%value%]
	 *
	 * @param value
	 * @return
	 */
	public abstract Object hValue(Object value);

	private static Object addAll(final Object value) {
		final Object v2 = addString(value);
		final Object v3 = addDate_Time_Timestamp(v2);
		return v3;
	}

	private static Object addDate_Time_Timestamp(final Object value) {
		checkArray(value);
		if ((value instanceof Date) || (value instanceof java.sql.Date) || (value instanceof Time)
				|| (value instanceof Timestamp)) {
			return "'" + value + "'";
		}
		return value;
	}

	private static Object checkArray(final Object value) {

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

	private static Object addString(final Object value) {
		checkArray(value);
		if ((value instanceof Character) || (value instanceof String)) {
			return "'" + value + "'";
		}

		return value;
	}

	private static Object inAndNotIn(final Object value) {
		if (!(value instanceof Iterable)) {
			final String m = "参数必须是" + Iterable.class.getSimpleName() + "类型,当前类型为[" + value.getClass().getCanonicalName()
					+ "]";
			throw new ZRWrapperTypeException(m);
		}

		final Iterable<?> i = (Iterable<?>) value;
		final StringJoiner joiner = new StringJoiner(",", "(", ")");
		for (final Object v : i) {
			joiner.add(String.valueOf(v));
		}

		return joiner.toString();
	}
}
