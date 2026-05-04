package com.vo.repository.core;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * 解析声明式方法名称的中间结果
 *
 * @author zhangzhen
 * @data 2024年5月3日 下午7:36:42
 *
 */
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

	public String getMethodName() {
		return methodName;
	}

	public void setMethodName(final String methodName) {
		this.methodName = methodName;
	}

	public List<String> getFiledName() {
		return filedName;
	}

	public void setFiledName(final List<String> filedName) {
		this.filedName = filedName;
	}

	public List<String> getFiledNameOriginalOrder() {
		return filedNameOriginalOrder;
	}

	public void setFiledNameOriginalOrder(final List<String> filedNameOriginalOrder) {
		this.filedNameOriginalOrder = filedNameOriginalOrder;
	}

	public List<String> getFiledNameMethodNameOrder() {
		return filedNameMethodNameOrder;
	}

	public void setFiledNameMethodNameOrder(final List<String> filedNameMethodNameOrder) {
		this.filedNameMethodNameOrder = filedNameMethodNameOrder;
	}

	public String getSqlKeyword() {
		return sqlKeyword;
	}

	public void setSqlKeyword(final String sqlKeyword) {
		this.sqlKeyword = sqlKeyword;
	}

	public D(final String methodName, final List<String> filedName, final List<String> filedNameOriginalOrder,
			final List<String> filedNameMethodNameOrder, final String sqlKeyword) {
		super();
		this.methodName = methodName;
		this.filedName = filedName;
		this.filedNameOriginalOrder = filedNameOriginalOrder;
		this.filedNameMethodNameOrder = filedNameMethodNameOrder;
		this.sqlKeyword = sqlKeyword;
	}

	public D() {
		super();
	}
	
}
