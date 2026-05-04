package com.vo.repository.core;


/**
 * 声明式/自定义方法 解析的SQL
 *
 * @author zhangzhen
 * @data 2024年5月5日 下午10:12:34
 *
 */
public class MethodSQL {

	/**
	 * 是否 @ZQuery 自定义SQL的方法
	 */
	private boolean isZQuery;

	/**
	 * 声明式方法名称/自定义方法名称
	 */
	private String methodName;

	/**
	 * SQL模板/自定义SQL
	 */
	private String sqlTemplate;

	public MethodSQL(final boolean isZQuery, final String methodName, final String sqlTemplate) {
		super();
		this.isZQuery = isZQuery;
		this.methodName = methodName;
		this.sqlTemplate = sqlTemplate;
	}

	public MethodSQL() {
		super();
	}

	public boolean isZQuery() {
		return isZQuery;
	}

	public void setZQuery(final boolean isZQuery) {
		this.isZQuery = isZQuery;
	}

	public String getMethodName() {
		return methodName;
	}

	public void setMethodName(final String methodName) {
		this.methodName = methodName;
	}

	public String getSqlTemplate() {
		return sqlTemplate;
	}

	public void setSqlTemplate(final String sqlTemplate) {
		this.sqlTemplate = sqlTemplate;
	}
	
}
