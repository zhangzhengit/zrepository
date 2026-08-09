package vo.repository.actuator;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

import vo.repository.anno.ZDateFormat;
import vo.repository.anno.ZDateFormatEnum;
import vo.repository.anno.ZEntity;
import vo.repository.anno.ZID;

/**
 *
 *
 * @author zhangzhen
 * @date 2024年6月2日 下午9:37:48
 *
 */
@ZEntity(tableName = "sql_invocation_logs", dataSourceName = "zdatasource_actuator.properties")
// FIXME 2025年8月25日 下午4:11:34 zhangzhen: 加入记录 sql的参数值 
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

	/**
	 * SQL 的参数值，多个的话用,分隔开
	 */
	String value;

	public SqlInvocationLogsEntity(final Integer id, final String zrSubClassName, final String methodName, final String sql,
			final Integer timeConsuming, final Date invokeTime, final String tableName, final String value) {
		super();
		this.id = id;
		this.zrSubClassName = zrSubClassName;
		this.methodName = methodName;
		this.sql = sql;
		this.timeConsuming = timeConsuming;
		this.invokeTime = invokeTime;
		this.tableName = tableName;
		this.value = value;
	}

	public SqlInvocationLogsEntity() {
		super();
	}

	public Integer getId() {
		return id;
	}

	public void setId(final Integer id) {
		this.id = id;
	}

	public String getZrSubClassName() {
		return zrSubClassName;
	}

	public void setZrSubClassName(final String zrSubClassName) {
		this.zrSubClassName = zrSubClassName;
	}

	public String getMethodName() {
		return methodName;
	}

	public void setMethodName(final String methodName) {
		this.methodName = methodName;
	}

	public String getSql() {
		return sql;
	}

	public void setSql(final String sql) {
		this.sql = sql;
	}

	public Integer getTimeConsuming() {
		return timeConsuming;
	}

	public void setTimeConsuming(final Integer timeConsuming) {
		this.timeConsuming = timeConsuming;
	}

	public Date getInvokeTime() {
		return invokeTime;
	}

	public void setInvokeTime(final Date invokeTime) {
		this.invokeTime = invokeTime;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(final String tableName) {
		this.tableName = tableName;
	}

	public String getValue() {
		return value;
	}

	public void setValue(final String value) {
		this.value = value;
	}
	
}
