package com.vo.anno;

/**
 * 分表策略
 *
 * @author zhangzhen
 * @date 2024年11月30日 上午11:54:55
 *
 */
public enum ZShardingStrategyEnum {

	TIME,

	RANGE,

	HASH,

	CONSISTENT_HASHING;

}
