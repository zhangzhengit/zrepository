package com.vo.transaction;

import java.sql.Connection;

/**
 * 事务隔离级别 @see java.sql.Connection
 *
 * @author zhangzhen
 * @date 2024年7月2日 下午11:02:51
 *
 */
public enum ZIsolationEnum {

	DEFAULT(-1),

	READ_UNCOMMITTED(Connection.TRANSACTION_READ_UNCOMMITTED),

	READ_COMMITTED(Connection.TRANSACTION_READ_COMMITTED),

	REPEATABLE_READ(Connection.TRANSACTION_REPEATABLE_READ),

	SERIALIZABLE(Connection.TRANSACTION_SERIALIZABLE),;

	public static ZIsolationEnum valueOfIsolation(final int isolation) {
		if (REPEATABLE_READ.getIsolation() == isolation) {
			return REPEATABLE_READ;
		}
		if (READ_COMMITTED.getIsolation() == isolation) {
			return READ_COMMITTED;
		}
		if (SERIALIZABLE.getIsolation() == isolation) {
			return SERIALIZABLE;
		}
		if (READ_UNCOMMITTED.getIsolation() == isolation) {
			return READ_UNCOMMITTED;
		}

		return DEFAULT;
	}

	public static boolean containsValue(final int isolation) {
		return (REPEATABLE_READ.getIsolation() == isolation) || (SERIALIZABLE.getIsolation() == isolation)
				|| (READ_COMMITTED.getIsolation() == isolation) || (READ_UNCOMMITTED.getIsolation() == isolation);
	}

	private final int isolation;

	public int getIsolation() {
		return isolation;
	}

	private ZIsolationEnum(final int isolation) {
		this.isolation = isolation;
	}
	
}
