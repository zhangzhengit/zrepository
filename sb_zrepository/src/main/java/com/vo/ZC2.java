package com.vo;

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


}
