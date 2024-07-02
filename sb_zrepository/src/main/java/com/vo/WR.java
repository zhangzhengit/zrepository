package com.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ZRWrapper 组装所有条件后生成的结果
 *
 * @author zhangzhen
 * @date 2024年6月30日 下午2:58:25
 *
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class WR {

	/**
	 * 完整的一个整个SQL
	 * 如：
	 * 	SELECT * FROM user WHERE name = ? ORDER BY create_time DESC LIMIT 20 OFFSET 0;
	 */
	private String sql;

	/**
	 * 完整的WHERE部分，包括后面的ORDER BY 和LIMIT部分
	 * 如：
	 * 	WHERE name = ? ORDER BY create_time DESC LIMIT 20 OFFSET 0
	 */
	private String fullWhere;

	/**
	 * WHERE部分，只包括WHERE部分，条件过滤的部分
	 * 如：
	 * 	WHERE name = ?
	 */
	private String where;

	/**
	 * ORDER BY 部分
	 * 如：
	 * 	ORDER BY create_time DESC
	 */
	private String orderBy;

	/**
	 * LIMIT部分
	 * 如：
	 * 	LIMIT 20 OFFSET 0
	 */
	private String limit;

}
