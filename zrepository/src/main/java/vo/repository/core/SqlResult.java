package vo.repository.core;

/**
 *
 *
 * @author zhangzhen
 * @date 2023年9月5日
 *
 */
public class SqlResult {

	/**
	 * ZRepository 子类名称
	 */
	private final String zRepositorySubClassName;

	/**
	 * 方法名，如：findByUserId
	 */
	private final String methodName;

	/**
	 * 最终生成的sql，如 select * from user where user_id = ?
	 */
	private final String sqlFinal;

	public SqlResult(final String zRepositorySubClassName, final String methodName, final String sqlFinal) {
		super();
		this.zRepositorySubClassName = zRepositorySubClassName;
		this.methodName = methodName;
		this.sqlFinal = sqlFinal;
	}

	public String getZRepositorySubClassName() {
		return zRepositorySubClassName;
	}

	public String getMethodName() {
		return methodName;
	}

	public String getSqlFinal() {
		return sqlFinal;
	}
	
}
