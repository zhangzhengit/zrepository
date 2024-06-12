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

	EQ("=", "等于"),

	NE("!=", "不等于"),

	LT("<", "小于"),

	LTE("<=", "小于等于"),

	GT(">", "大于"),

	GTE(">=", "大于等于"),

	;

	private String content;
	private String remark;

}
