package com.vo;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 * 解析声明式方法名称的中间结果
 *
 * @author zhangzhen
 * @data 2024年5月3日 下午7:36:42
 *
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class D {

	/**
	 * 声明式方法的名称，用户声明的是什么，本字段就是什么，如： findByIsDelete
	 */
	private String methodName;

	/**
	 * Entity中的字段名称数组，如:{"isDelete"}：
	 *
	 * 注意：这个是按Class中的Field.getName().length()倒序排序的
	 *
	 */
	private List<String> filedName = new ArrayList<>();

	/**
	 * 本字段与 filedName 不同是，本字段没排序过，顺序按 Class.getDeclaredFields() 来存放
	 */
	private List<String> filedNameOriginalOrder;
	
	/**
	 * 本字段顺序，按声明式方法名称中出现的字段名称来排序，如 findByDateOrderByNameDescLimit
	 * 则本字段值为[date,name]
	 */
	private List<String> filedNameMethodNameOrder;

	/**
	 * methodName 去除 了filedName后的结果，如：findBy
	 */
	private String sqlKeyword;

	public void addFiledName(final String filedName) {
		this.getFiledName().add(filedName);
	}
}
