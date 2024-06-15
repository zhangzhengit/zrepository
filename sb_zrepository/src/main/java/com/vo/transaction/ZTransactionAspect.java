package com.vo.transaction;

import java.lang.reflect.Method;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import com.vo.anno.ZRead;
import com.vo.conn.Mode;
import com.vo.conn.ZCPool;
import com.vo.conn.ZConnection;
import com.vo.conn.ZDatasourcePropertiesLoader;


/**
 * 拦截 @ZTransaction 标记的方法,执行成功后提交事务，出现异常则回滚事务
 *
 * @author zhangzhen
 * @date 2023年6月17日
 *
 */
@Aspect
@Component
public class ZTransactionAspect {

	/**
	 * 	@ZTransaction 方法执行前把Connection放在这，具体的方法从这里拿到Connection,
	 *  即使@ZTransaction 方法里嵌套@ZTransaction 方法，也是用的同一个Connection来执行
	 */
	public static final ThreadLocal<ZConnection> ZCONNECTION_THREADLOCAL = new ThreadLocal<>();

	/**
	 * 回滚当前事务
	 */
	public static void rollback() {
		ZCONNECTION_THREADLOCAL.get().rollback();
	}

	public static void commit() {
		ZCONNECTION_THREADLOCAL.get().commit();
	}

	@Around(value = "pointcut()")
	public final Object around(final ProceedingJoinPoint proceedingJoinPoint) {
		final MethodSignature ms = (MethodSignature) proceedingJoinPoint.getSignature();
		final Method method = ms.getMethod();

		// FIXME 2024年5月31日 下午5:24:42 zhangzhen :
		// 考虑好，如果依赖的数据源名称不是zdatasource.properties要怎么办？
		// 提前把Method和dataSourceName存起来，在此根据Method取出？
		final String defaultDatsourceName = ZDatasourcePropertiesLoader.DEFAULT_DATSOURCE_NAME;
		final ZCPool c = ZCPool.getInstance(defaultDatsourceName);
		final ZConnection zConnection = c.getZConnection(method.isAnnotationPresent(ZRead.class) ? Mode.READ : Mode.WRITE);
		ZCONNECTION_THREADLOCAL.set(zConnection);

		try {
			zConnection.getConnection().setAutoCommit(false);
			final Object v = proceedingJoinPoint.proceed();
			return v;
		} catch (final Throwable e) {
			ZCONNECTION_THREADLOCAL.get().rollback();
			e.printStackTrace();
		} finally {
			ZCONNECTION_THREADLOCAL.get().commit();
			ZCPool.getInstance(defaultDatsourceName).returnZConnectionAndCommit(ZCONNECTION_THREADLOCAL.get());
		}

		return null;
	}

	@Pointcut("@annotation(com.vo.transaction.ZTransaction)")
	public void pointcut() {
		System.out.println(java.time.LocalDateTime.now() + "\t" + Thread.currentThread().getName() + "\t"
				+ "ZTransactionAspect.pointcut()");

	}
}
