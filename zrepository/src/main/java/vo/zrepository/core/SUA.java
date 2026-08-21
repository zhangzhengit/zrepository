package vo.zrepository.core;

import java.lang.annotation.Annotation;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 *
 * @author zhangzhen
 * @date 2024年6月18日 下午3:42:01
 *
 */
public class SUA {

	private static final ConcurrentHashMap<Class<?>, Field[] > CLASS_DF_CACHE = new ConcurrentHashMap<>(16, 1F);


	Class<?> entityClass;
	private final Field[] declaredFields;
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

		this.declaredFields = CLASS_DF_CACHE.computeIfAbsent(entityClass, Class::getDeclaredFields);

		this.entityObject = entityObject;
		this.returnClass = returnClass;
		this.sql = sql;
		this.arg = arg;
	}

	public Class<?> getEntityClass() {
		return this.entityClass;
	}

	public void setEntityClass(final Class<?> entityClass) {
		this.entityClass = entityClass;
	}

	public Object getEntityObject() {
		return this.entityObject;
	}

	public void setEntityObject(final Object entityObject) {
		this.entityObject = entityObject;
	}

	public Class<?> getReturnClass() {
		return this.returnClass;
	}

	public void setReturnClass(final Class<?> returnClass) {
		this.returnClass = returnClass;
	}

	public String getSql() {
		return this.sql;
	}

	public void setSql(final String sql) {
		this.sql = sql;
	}

	public Object[] getArg() {
		return this.arg;
	}

	public void setArg(final Object[] arg) {
		this.arg = arg;
	}

	public ZC2 getZc2() {
		return this.zc2;
	}

	public void setZc2(final ZC2 zc2) {
		this.zc2 = zc2;
	}

	public String getZrSubClassName() {
		return this.zrSubClassName;
	}

	public void setZrSubClassName(final String zrSubClassName) {
		this.zrSubClassName = zrSubClassName;
	}

	public String getCallerMethodName() {
		return this.callerMethodName;
	}

	public void setCallerMethodName(final String callerMethodName) {
		this.callerMethodName = callerMethodName;
	}

	public String getWhere() {
		return this.where;
	}

	public void setWhere(final String where) {
		this.where = where;
	}

	public Field[] getDeclaredFields() {
		return this.declaredFields;
	}

	public Field isAnyFieldHasAnnotation(final Class<? extends Annotation> annoClass) {
		final Field[] fs = this.getDeclaredFields();
		for (final Field field : fs) {
			if (field.isAnnotationPresent(annoClass)) {
				return field;
			}
		}

		return null;
	}

}
