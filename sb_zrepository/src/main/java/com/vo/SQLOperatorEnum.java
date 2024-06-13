package com.vo;

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
			return value;
		}
	},

	NE("!=", "不等于") {
		@Override
		public Object hValue(final Object value) {
			return value;
		}
	},

	LT("<", "小于") {
		@Override
		public Object hValue(final Object value) {
			return value;
		}
	},

	LTE("<=", "小于等于") {
		@Override
		public Object hValue(final Object value) {
			return value;
		}
	},

	GT(">", "大于") {
		@Override
		public Object hValue(final Object value) {
			return value;
		}
	},

	GTE(">=", "大于等于") {
		@Override
		public Object hValue(final Object value) {
			return value;
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

			return "%" + value + "%";
		}
	},

	NOT_LIKE("NOT LIKE", "模糊查询:非") {
		@Override
		public Object hValue(final Object value) {
			return "%" + value + "%";
		}
	},

	IS_NULL("IS NULL", "判断某个column为null") {
		@Override
		public Object hValue(final Object value) {
			return value;
		}
	},

	NOT_NULL("NOT NULL", "判断某个column为not null") {
		@Override
		public Object hValue(final Object value) {
			return value;
		}
	},

	ENDING_WITH("LIKE", "模糊查询:匹配后缀") {
		@Override
		public Object hValue(final Object value) {
			return value + "%";
		}
	},

	STARTING_WITH("LIKE", "模糊查询:匹配前缀") {
		@Override
		public Object hValue(final Object value) {
			return "%" + value;
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

}
