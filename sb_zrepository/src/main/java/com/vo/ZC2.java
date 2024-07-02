package com.vo;

import java.util.ArrayList;
import java.util.List;

import com.vo.conn.ZConnection;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 *
 *
 * @author zhangzhen
 * @date 2024年6月15日 上午10:10:52
 *
 */
@Data
@AllArgsConstructor
public class ZC2 {

	private final ZConnection zConnection;

	private final ZCSourceEnum sourceEnum;

	/**
	 * 事务ID，只有在 sourceEnum == ZTRANSACTION 时，才给本字段赋值，表示一个事务，给本字段初始化一个唯一的值
	 */
	private String transactionId;

	private List<String> keyList = new ArrayList<>();

	public void addKey(final String key) {
		this.keyList.add(key);
	}

	public ZC2(final ZConnection zConnection, final ZCSourceEnum sourceEnum, final String transactionId) {
		this.zConnection = zConnection;
		this.sourceEnum = sourceEnum;
		this.transactionId = transactionId;
	}

}
