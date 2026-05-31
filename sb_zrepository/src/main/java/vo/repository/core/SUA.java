package vo.repository.core;

/**
 *
 *
 * @author zhangzhen
 * @date 2024年6月18日 下午3:42:01
 *
 */
public class SUA {

	Class<?> entityClass;
	Object entityObject;

	Class<?> returnClass;
	String sql;
	Object[] arg;

	ZC2 zc2;

	String zrSubClassName;
	String callerMethodName;

	/**
	 * hanlder处理后返回的部分where 条件，如： name = ?
	 */
	private String where;

	public SUA(final Class<?> entityClass, final Object entityObject, final Class<?> returnClass, final String sql, final Object[] arg) {
		this.entityClass = entityClass;
		this.entityObject = entityObject;
		this.returnClass = returnClass;
		this.sql = sql;
		this.arg = arg;
	}

	public SUA(final Class<?> entityClass, final Object entityObject, final Class<?> returnClass, final String sql, final Object[] arg, final ZC2 zc2,
			final String zrSubClassName, final String callerMethodName, final String where) {
		super();
		this.entityClass = entityClass;
		this.entityObject = entityObject;
		this.returnClass = returnClass;
		this.sql = sql;
		this.arg = arg;
		this.zc2 = zc2;
		this.zrSubClassName = zrSubClassName;
		this.callerMethodName = callerMethodName;
		this.where = where;
	}

	public Class<?> getEntityClass() {
		return entityClass;
	}

	public void setEntityClass(final Class<?> entityClass) {
		this.entityClass = entityClass;
	}

	public Object getEntityObject() {
		return entityObject;
	}

	public void setEntityObject(final Object entityObject) {
		this.entityObject = entityObject;
	}

	public Class<?> getReturnClass() {
		return returnClass;
	}

	public void setReturnClass(final Class<?> returnClass) {
		this.returnClass = returnClass;
	}

	public String getSql() {
		return sql;
	}

	public void setSql(final String sql) {
		this.sql = sql;
	}

	public Object[] getArg() {
		return arg;
	}

	public void setArg(final Object[] arg) {
		this.arg = arg;
	}

	public ZC2 getZc2() {
		return zc2;
	}

	public void setZc2(final ZC2 zc2) {
		this.zc2 = zc2;
	}

	public String getZrSubClassName() {
		return zrSubClassName;
	}

	public void setZrSubClassName(final String zrSubClassName) {
		this.zrSubClassName = zrSubClassName;
	}

	public String getCallerMethodName() {
		return callerMethodName;
	}

	public void setCallerMethodName(final String callerMethodName) {
		this.callerMethodName = callerMethodName;
	}

	public String getWhere() {
		return where;
	}

	public void setWhere(final String where) {
		this.where = where;
	}
	
}
