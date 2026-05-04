package com.vo.repository.core;

/**
 * ZRWrapper 组装所有条件后生成的结果
 *
 * @author zhangzhen
 * @date 2024年6月30日 下午2:58:25
 *
 */
public class WR {

	/**
	 * 完整的一个整个SQL
	 * 如：
	 * 	SELECT * FROM user WHERE name = ? ORDER BY create_time DESC LIMIT 20 OFFSET 0;
	 */
	private String sql;

	/**
	 * 完整的WHERE部分，包括后面的ORDER BY 和LIMIT部分
	 * 如：
	 * 	WHERE name = ? ORDER BY create_time DESC LIMIT 20 OFFSET 0
	 */
	private String fullWhere;

	/**
	 * WHERE部分，只包括WHERE部分，条件过滤的部分
	 * 如：
	 * 	WHERE name = ?
	 */
	private String where;

	/**
	 * ORDER BY 部分
	 * 如：
	 * 	ORDER BY create_time DESC
	 */
	private String orderBy;

	/**
	 * LIMIT部分
	 * 如：
	 * 	LIMIT 20 OFFSET 0
	 */
	private String limit;

	public WR(final String sql, final String fullWhere, final String where, final String orderBy, final String limit) {
		super();
		this.sql = sql;
		this.fullWhere = fullWhere;
		this.where = where;
		this.orderBy = orderBy;
		this.limit = limit;
	}

	public String getSql() {
		return sql;
	}

	public void setSql(final String sql) {
		this.sql = sql;
	}

	public String getFullWhere() {
		return fullWhere;
	}

	public void setFullWhere(final String fullWhere) {
		this.fullWhere = fullWhere;
	}

	public String getWhere() {
		return where;
	}

	public void setWhere(final String where) {
		this.where = where;
	}

	public String getOrderBy() {
		return orderBy;
	}

	public void setOrderBy(final String orderBy) {
		this.orderBy = orderBy;
	}

	public String getLimit() {
		return limit;
	}

	public void setLimit(final String limit) {
		this.limit = limit;
	}

	public WR() {
		super();
	}
	
}
