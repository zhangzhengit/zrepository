package com.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 构造查询条件的键值对
 *
 * @author zhangzhen
 * @date 2024年6月12日 下午7:47:08
 *
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class WrapperPair<T> {

	SerializableFunction<T, Object> function;
	Object value;

}
