package com.vo.repository.core;

import java.util.ArrayList;
import java.util.List;

import com.vo.repository.anno.ZCSourceEnum;
import com.vo.repository.conn.ZConnection;
import com.vo.repository.transaction.ZIsolationEnum;

/**
 *
 *
 * @author zhangzhen
 * @date 2024年6月15日 上午10:10:52
 *
 */
public class ZC2 {

	private ZConnection zConnection;

	private ZCSourceEnum sourceEnum;

	private ZIsolationEnum isolationEnum;

	/**
	 * 事务ID，只有在 sourceEnum == ZTRANSACTION 时，才给本字段赋值，表示一个事务，给本字段初始化一个唯一的值
	 */
	private String transactionId;

	private List<String> keyList = new ArrayList<>();

	public void addKey(final String key) {
		this.keyList.add(key);
	}

	public ZC2(final ZConnection zConnection, final ZCSourceEnum sourceEnum) {
		this(zConnection, sourceEnum, null);
	}

	public ZC2(final ZConnection zConnection, final ZCSourceEnum sourceEnum, final String transactionId) {
		this.zConnection = zConnection;
		this.sourceEnum = sourceEnum;
		this.transactionId = transactionId;
	}

	public ZIsolationEnum getIsolationEnum() {
		return isolationEnum;
	}

	public void setIsolationEnum(final ZIsolationEnum isolationEnum) {
		this.isolationEnum = isolationEnum;
	}

	public String getTransactionId() {
		return transactionId;
	}

	public void setTransactionId(final String transactionId) {
		this.transactionId = transactionId;
	}

	public List<String> getKeyList() {
		return keyList;
	}

	public void setKeyList(final List<String> keyList) {
		this.keyList = keyList;
	}

	public ZConnection getZConnection() {
		return zConnection;
	}

	public ZCSourceEnum getSourceEnum() {
		return sourceEnum;
	}

	public ZC2(final ZConnection zConnection, final ZCSourceEnum sourceEnum, final ZIsolationEnum isolationEnum, final String transactionId,
			final List<String> keyList) {
		super();
		this.zConnection = zConnection;
		this.sourceEnum = sourceEnum;
		this.isolationEnum = isolationEnum;
		this.transactionId = transactionId;
		this.keyList = keyList;
	}
	
}
