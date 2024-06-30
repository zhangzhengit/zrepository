package com.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ZRWrapper 生成的结果
 *
 * @author zhangzhen
 * @date 2024年6月30日 下午2:58:25
 *
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class WR {

	private String sql;
	/**
	 * 完整的where部分，包括后面的order by 和limit部分
	 */
	private String fullWhere;
	private String where;
	private String orderBy;
	private String limit;

}
