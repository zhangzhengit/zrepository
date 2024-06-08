package com.vo;

import java.util.HashSet;

import com.google.common.collect.Sets;

import cn.hutool.core.util.StrUtil;

/**
 * 数据库字段 <> java对象字段转换, 如： order_count <> orderCount
 *
 * @author zhangzhen
 * @date 2023年6月16日
 *
 */
public class ZFieldConverter {

	// FIXME 2024年5月27日 下午8:50:47 zhangzhen: 这个要好好测，万一出了问题，容易导致认为是后续代码的问题

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
		final char[] charArray = dbFieldName.toCharArray();

		String n = dbFieldName;
		for (int i = 0; i < charArray.length; i++) {
			final char c = charArray[i];
			if (c == '_') {
				final char nextC = charArray[i + 1];
				n = n.replace("_" + nextC, String.valueOf(nextC).toUpperCase());

			}
		}

		return n;
	}

	public static String toDbField(final String javaFieldName) {
		if (StrUtil.isEmpty(javaFieldName)) {
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
			if (daxie.contains(c)) {
				if (i == 0) {
					n.replace(i + count, i + count + 1, String.valueOf(c).toLowerCase());
				} else {
					n.replace(i + count, i + count + 1, "_" + String.valueOf(c).toLowerCase());
				}
				count++;
			}
		}

		return n.toString();
	}

	static HashSet<Character> daxie = Sets.newHashSet('A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
			'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z');

}
