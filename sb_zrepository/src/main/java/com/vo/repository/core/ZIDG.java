package com.vo.repository.core;

import java.util.UUID;

/**
 * ID 生成器
 *
 * @author zhangzhen
 * @date 2024年7月2日 下午9:10:22
 *
 */
public class ZIDG {

	/**
	 * 使用UUID生成
	 *
	 * @return
	 */
	public static String g() {
		final String id = UUID.randomUUID().toString();
		return id;
	}

}
