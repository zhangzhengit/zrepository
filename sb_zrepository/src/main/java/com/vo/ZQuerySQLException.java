package com.vo;

import java.sql.SQLException;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @ZQuery 自定义SQL异常
 *
 * @author zhangzhen
 * @data 2024年5月10日 下午8:53:58
 *
 */
@Data
@NoArgsConstructor
public class ZQuerySQLException extends SQLException {

	private static final long serialVersionUID = 1L;

	private String message;

	public ZQuerySQLException(final String message) {
		this.message = message;
	}

}
