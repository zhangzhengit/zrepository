package com.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 *
 * @author zhangzhen
 * @date 2024年6月18日 下午3:42:01
 *
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SUA {

	Class<?> entityClass;
	Object entityObject;

	Class<?> returnClass;
	String sql;
	Object[] arg;
}
