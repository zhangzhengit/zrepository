package com.vo.repository.core;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.vo.cache.STU;

/**
 * 数据库字段 <> java对象字段转换, 如： order_count <> orderCount
 *
 * @author zhangzhen
 * @date 2023年6月16日
 *
 */
public class ZFieldConverter {

	public static final String UNDERSCORE = "_";
	public static final Character UNDERSCORE_CHARACTER = '_';

	public final static ImmutableSet<Character> UPPERCASE_LETTER = ImmutableSet.copyOf(
			Sets.newHashSet('A', 'B', 'C', 'D', 'E', 'F',
					'G', 'H', 'I', 'J', 'K', 'L', 'M',
					'N', 'O', 'P', 'Q', 'R', 'S', 'T',
					'U', 'V', 'W', 'X', 'Y', 'Z'));

	/**
	 * Class中的Field.name 转为声明式方法中的一部分
	 * 如：参数传来 createTime，本方法返回CreateTime
	 *
	 * @param javaFieldName
	 * @return
	 */
	public static String toMethodName(final String javaFieldName) {
		final StringBuilder builder = new StringBuilder();
		builder.append(String.valueOf(javaFieldName.charAt(0)).toUpperCase());
		builder.append(javaFieldName.substring(1));
		return builder.toString();
	}

	public static String toJavaField(final String dbFieldName) {
		return toJavaField0(dbFieldName);
	}

	private static String toJavaField0(final String dbFieldName) {
		final char[] charArray = dbFieldName.toCharArray();

		String n = dbFieldName;
		for (int i = 0; i < charArray.length; i++) {
			final char c = charArray[i];
			if (c == UNDERSCORE_CHARACTER) {
				final char nextC = charArray[i + 1];
				n = n.replace(UNDERSCORE + nextC, String.valueOf(nextC).toUpperCase());
			}
		}

		return n;
	}

	public static String toDbField(final String javaFieldName) {
		if (STU.isEmpty(javaFieldName)) {
			throw new IllegalArgumentException("javaFieldName 不能为空");
		}
		if (javaFieldName.length() == 1) {
			return  String.valueOf(new char[] {javaFieldName.charAt(0)}).toLowerCase();
		}

		final StringBuilder x = new StringBuilder(javaFieldName).replace(0, 1,String.valueOf(new char[] {javaFieldName.charAt(0)}).toLowerCase());
		final char[] charArray = x.toString().toCharArray();
		int count = 0;
		final StringBuilder n = new StringBuilder(x);
		for (int i = 0; i < charArray.length; i++) {
			final char c = charArray[i];
			if (UPPERCASE_LETTER.contains(c)) {
				if (i == 0) {
					n.replace(i + count, i + count + 1, String.valueOf(c).toLowerCase());
				} else {
					n.replace(i + count, i + count + 1, UNDERSCORE + String.valueOf(c).toLowerCase());
				}
				count++;
			}
		}

		return n.toString();
	}

}
