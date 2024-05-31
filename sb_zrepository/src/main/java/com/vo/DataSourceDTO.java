package com.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 
 *
 * @author zhangzhen
 * @date 2024年6月1日 上午2:53:28
 * 
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DataSourceDTO {

	private String catalog;

	private DBEnum dbEnum;
}
