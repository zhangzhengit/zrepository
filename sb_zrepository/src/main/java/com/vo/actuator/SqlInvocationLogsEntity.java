package com.vo.actuator;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.vo.ZDateFormat;
import com.vo.ZDateFormatEnum;
import com.vo.ZID;
import com.vo.anno.ZEntity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 *
 * @author zhangzhen
 * @date 2024年6月2日 下午9:37:48
 *
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ZEntity(tableName = "sql_invocation_logs", dataSourceName = "zdatasource_sqlite.properties")
public class SqlInvocationLogsEntity {

	@ZID
	Integer id;

	/**
	 * ZRepository的ClassName
	 */
	String zrSubClassName;

	/**
	 * ZRepository的Method的Name
	 */
	String methodName;

	/**
	 * ZRepository中的Method生成的具体SQL
	 */
	String sql;

	/**
	 * 此 ZRepository的Method执行耗时，单位：毫秒
	 */
	Integer timeConsuming;

	/**
	 * 此 ZRepository的Method执行的时间点
	 */
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
	@ZDateFormat(format = ZDateFormatEnum.YYYY_MM_DD_HH_MM_SS)
	Date invokeTime;

	/**
	 * 此sql执行时对应的table的名称
	 */
	String tableName;
}
