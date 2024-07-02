package com.vo.transaction;

import java.lang.reflect.Method;
import java.sql.SQLException;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import com.vo.ZC2;
import com.vo.ZCSourceEnum;
import com.vo.ZIDG;
import com.vo.ZRC;
import com.vo.anno.ZRead;
import com.vo.aop.AOPParameter;
import com.vo.aop.ZAOP;
import com.vo.aop.ZIAOP;
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
@ZAOP(interceptType = ZTransaction.class)
public class ZTransactionAOP implements ZIAOP {

	/**
	 * 	@ZTransaction 方法执行前把Connection放在这，具体的方法从这里拿到Connection,
	 *  即使@ZTransaction 方法里嵌套@ZTransaction 方法，也是用的同一个Connection来执行
	 */
	private static final ThreadLocal<ZC2> ZCONNECTION_THREADLOCAL = new ThreadLocal<>();

	/**
	 * 获取当前 ZConnection 独对象
	 * 仅在加入了 @ZTransaction 注解的方法里的当前线程下可以获取到，
	 * 否则会返回null
	 *
	 * @return
	 */
	public static ZC2 getCurrentZConnection() {
		return ZCONNECTION_THREADLOCAL.get();
	}

	/**
	 * 回滚当前事务
	 */
	public static void rollback() {
		try {
			ZCONNECTION_THREADLOCAL.get().getZConnection().getConnection().rollback();
		} catch (final SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 提交当前事务
	 */
	public static void commit() {
		try {
			ZCONNECTION_THREADLOCAL.get().getZConnection().getConnection().commit();
		} catch (final SQLException e) {
			e.printStackTrace();
		}
	}

	private static void clear() {
		ZCONNECTION_THREADLOCAL.set(null);
	}

	@Around(value = "pointcut()")
	public final Object around(final ProceedingJoinPoint proceedingJoinPoint) {
		final Method method = ((MethodSignature) proceedingJoinPoint.getSignature()).getMethod();

		// FIXME 2024年5月31日 下午5:24:42 zhangzhen :
		// 考虑好，如果依赖的数据源名称不是zdatasource.properties要怎么办？
		// 提前把Method和dataSourceName存起来，在此根据Method取出？
		final String defaultDatsourceName = ZDatasourcePropertiesLoader.DEFAULT_DATSOURCE_NAME;
		final ZCPool c = ZCPool.getInstance(defaultDatsourceName);
		final ZConnection zConnection = c.getZConnection(method.isAnnotationPresent(ZRead.class) ? Mode.READ : Mode.WRITE);
		final ZC2 zc2 = new ZC2(zConnection, ZCSourceEnum.ZTRANSACTION, ZIDG.g());
		ZCONNECTION_THREADLOCAL.set(zc2);

		try {
			zConnection.getConnection().setAutoCommit(false);
			final Object v = proceedingJoinPoint.proceed();
			return v;
		} catch (final Throwable e) {
			try {
				ZCONNECTION_THREADLOCAL.get().getZConnection().getConnection().rollback();
			} catch (final SQLException e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
		} finally {
			try {
				ZCONNECTION_THREADLOCAL.get().getZConnection().getConnection().commit();
			} catch (final SQLException e) {
				e.printStackTrace();
			}
			ZCPool.getInstance(defaultDatsourceName).returnZConnectionAndCommit(ZCONNECTION_THREADLOCAL.get().getZConnection());

			clear();
			ZRC.clear(zc2.getKeyList());
		}

		return null;
	}

	@Pointcut("@annotation(com.vo.transaction.ZTransaction)")
	public void pointcut() {

	}

	@Override
	public Object before(final AOPParameter aopParameter) {
		return null;
	}

	/**
	 * zframework的 AOP方法，拦截：@ZAOP(interceptType = ZTransaction.class)
	 * 本方法逻辑和springAOP public final Object around(final ProceedingJoinPoint proceedingJoinPoint)
	 * 方法逻辑一致，都是在[一个或一组操作DB的目标方法]执行前获取一个ZConnection，然后这一组操作都使用
	 * 这一个ZConnection，全部执行成功则commit，有一个执行异常则rollback.
	 *
	 */
	@Override
	public Object around(final AOPParameter aopParameter) {

		final Method method = aopParameter.getMethod();

		final String defaultDatsourceName = ZDatasourcePropertiesLoader.DEFAULT_DATSOURCE_NAME;
		final ZCPool c = ZCPool.getInstance(defaultDatsourceName);
		final ZConnection zConnection = c
				.getZConnection(method.isAnnotationPresent(ZRead.class) ? Mode.READ : Mode.WRITE);
		final ZC2 zc2 = new ZC2(zConnection, ZCSourceEnum.ZTRANSACTION, ZIDG.g());
		ZCONNECTION_THREADLOCAL.set(zc2);

		try {
			zConnection.getConnection().setAutoCommit(false);
			final Object v = aopParameter.invoke();
			return v;
		} catch (final Throwable e) {
			try {
				ZCONNECTION_THREADLOCAL.get().getZConnection().getConnection().rollback();
			} catch (final SQLException e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
		} finally {
			try {
				ZCONNECTION_THREADLOCAL.get().getZConnection().getConnection().commit();
			} catch (final SQLException e) {
				e.printStackTrace();
			}
			ZCPool.getInstance(defaultDatsourceName).returnZConnectionAndCommit(ZCONNECTION_THREADLOCAL.get().getZConnection());

			clear();
			ZRC.clear(zc2.getKeyList());
		}

		return null;
	}

	@Override
	public Object after(final AOPParameter aopParameter) {
		return null;
	}
}
