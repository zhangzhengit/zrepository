package com.vo.transaction;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicReference;

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

	private final static AtomicReference<String> tl = new AtomicReference<>();

	/**
	 * 	@ZTransaction 方法执行前把Connection放在这，具体的方法从这里拿到Connection,
	 *  即使@ZTransaction 方法里嵌套@ZTransaction 方法，也是用的同一个Connection来执行
	 */
	public static final ThreadLocal<ZConnection> ZCONNECTION_THREADLOCAL = new ThreadLocal<>();

	@Around(value = "pointcut()")
	public final Object around(final ProceedingJoinPoint proceedingJoinPoint) {
		System.out.println(java.time.LocalDateTime.now() + "\t" + Thread.currentThread().getName() + "\t"
				+ "ZTransactionAspect.around()");

		final MethodSignature ms = (MethodSignature) proceedingJoinPoint.getSignature();
		final Method method = ms.getMethod();
		final boolean isRead = method.isAnnotationPresent(ZRead.class);
		final Mode mode = isRead ? Mode.READ : Mode.WRITE;
		// FIXME 2024年5月31日 下午5:24:42 zhangzhen : 考虑好，如果依赖的数据源名称不是zdatasource.properties要怎么办？
		// 提前把Method和dataSourceName存起来，在此根据Method取出？
		final String defaultDatsourceName = ZDatasourcePropertiesLoader.DEFAULT_DATSOURCE_NAME;
		final ZCPool i = ZCPool.getInstance(defaultDatsourceName);
		final ZConnection zc = i.getZConnection(mode);

		final Connection connection = zc.getConnection();
		try {
			connection.setAutoCommit(false);
		} catch (final SQLException e1) {
			e1.printStackTrace();
		} finally {
			ZCPool.getInstance(defaultDatsourceName).returnZConnectionAndCommit(zc);
		}

		ZCONNECTION_THREADLOCAL.set(zc);

		try {
			final Object v = proceedingJoinPoint.proceed();
			connection.commit();
			return v;
		} catch (final Throwable e) {
			try {
				connection.rollback();
			} catch (final SQLException e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
		} finally {
			ZCPool.getInstance(defaultDatsourceName).returnZConnectionAndCommit(zc);
		}

		return null;
	}

	@Pointcut("@annotation(com.vo.transaction.ZTransaction)")
	public void pointcut() {
		System.out.println(java.time.LocalDateTime.now() + "\t" + Thread.currentThread().getName() + "\t"
				+ "ZTransactionAspect.pointcut()");

	}
}
