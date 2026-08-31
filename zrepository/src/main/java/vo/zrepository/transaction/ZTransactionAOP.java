package vo.zrepository.transaction;

import java.lang.reflect.Method;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import vo.vortex.anno.ZAOP;
import vo.vortex.aop.AOPParameter;
import vo.vortex.aop.ZIAOP;
import vo.vortex.cache.ZRC;
import vo.zrepository.anno.ZCSourceEnum;
import vo.zrepository.anno.ZRead;
import vo.zrepository.conn.Mode;
import vo.zrepository.conn.ZCPool;
import vo.zrepository.conn.ZConnection;
import vo.zrepository.conn.ZDatasourcePropertiesLoader;
import vo.zrepository.core.ZC2;
import vo.zrepository.core.ZIDG;
import vo.zrepository.enums.DBEnum;


/**
 * 拦截 @ZTransaction 标记的方法,
 * 执行成功后提交事务，出现异常则回滚事务
 *
 * 整体流程：
 * 1、在目标方法执行前，取一个java.sql.Connection对象，
 *
 * 2、在try里执行：
 * 	  根据 @ZTransaction.isolation 配置本事务的隔离级别
 * 	  设置非自动提交，放在ThreadLocal里，开始执行目标方法
 *
 * 3、各个目标方法(SU中的各个方法)都优先从本类[getCurrentZConnection]方法取连接对象:
 * 		1、取到了则说明当前方法(目标方法)是事务控制，则目标方法下的每个
	  	   ZRepository 里的操作取的都是同一个连接对象。所有操作都用这
	  	   同一个连接对象来执行。
	  	2、没取到连接，则继续从连接池中取然后执行后面流程。没取到则说明模板方法不是由 @ZTransaction 控制，则
 	  	   在目标方法执行的整个过程中，本类的[around]方法都不会执行，即本类不参与目标
    	   方法的Connection对象管理过程，getCurrentZConnection 只作为一个普通的静态方法来使用，本文档的
    	   5个步骤都不会执行，也就没有前面的配置事务隔离级别和后面的回滚/提交事务等等
 *
 * 4、第3步执行正常结束，则在try代码块末尾执行 connection.commit() 来提交事务
 * 	  第3步执行出现异常(代码自动抛出异常/手动抛出异常)，
 * 			则try代码块剩下代码不再执行，直接跳往catch代码块里
 *    		执行 connection.rollback() 来回滚事务
 *
 * 5、最后执行finally代码块：重置连接隔离级别为默认值、归还连接对象、
 * 	  清除 ThreadLocal 的值、清除事务内缓存值 等等
 * 	  到此，本类的 @ZTransaction 事务控制流程结束
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
	private static void rollback() {
		ZCONNECTION_THREADLOCAL.get().getZConnection().rollback();
	}

	public static void setZConnection(final ZC2 zc2) {
		ZCONNECTION_THREADLOCAL.set(zc2);
	}


	/**
	 * 提交当前事务
	 */
	public static void commit() {
		ZCONNECTION_THREADLOCAL.get().getZConnection().commitIfAutoCommitFalse();
	}

	private static void clear() {
		ZCONNECTION_THREADLOCAL.set(null);
	}

	@Around(value = "pointcut()")
	public final Object around(final ProceedingJoinPoint proceedingJoinPoint) {

		final Method method = ((MethodSignature) proceedingJoinPoint.getSignature()).getMethod();

		final String defaultDatsourceName = ZTransactionAOP.before(method);

		try {
			ZCONNECTION_THREADLOCAL.get().getZConnection().setAutoCommitFalse();

			final Object v = proceedingJoinPoint.proceed();
			commit();
			return v;
		} catch (final Throwable e) {
			e.printStackTrace();
			rollback();
		} finally {

			resetToDefaultTransactionIsolation(ZCONNECTION_THREADLOCAL.get());
			ZCONNECTION_THREADLOCAL.get().getZConnection().setAutoCommitTrue();

			ZCPool.getInstance(defaultDatsourceName)
			.returnZConnectionAndCommit(ZCONNECTION_THREADLOCAL.get().getZConnection());

			ZRC.singleton().clear(ZCONNECTION_THREADLOCAL.get().getKeyList());

			clear();
		}

		return null;
	}

	private static String before(final Method method) {
		// FIXME 2024年5月31日 下午5:24:42 zhangzhen :
		// 考虑好，如果依赖的数据源名称不是zdatasource.properties要怎么办？
		// 提前把Method和dataSourceName存起来，在此根据Method取出？
		final String defaultDatsourceName = ZDatasourcePropertiesLoader.DEFAULT_DATSOURCE_NAME;
		final ZCPool c = ZCPool.getInstance(defaultDatsourceName);
		final ZConnection zConnection = c.getZConnection(method.isAnnotationPresent(ZRead.class) ? Mode.READ : Mode.WRITE);
		final ZC2 zc2 = new ZC2(zConnection, ZCSourceEnum.ZTRANSACTION, ZIDG.g());

		final ZTransaction zTransaction = method.getAnnotation(ZTransaction.class);

		final ZIsolationEnum isolationEnum = ((zTransaction == null)  || (zTransaction.isolation() == ZIsolationEnum.DEFAULT))?
				ZIsolationEnum.valueOfIsolation(zc2.getZConnection().getTransactionIsolation()) :
					zTransaction.isolation();
		zc2.setIsolationEnum(isolationEnum);

		// 加入 connection.setAutoCommit(true);
		// 这行是为了兼容pgsql，不加会偶发性报错：org.postgresql.util.PSQLException:
		// 不能在事务交易过程中改变事物交易隔绝等级。
		if (zc2.getZConnection().getDbEnum() == DBEnum.POSTGRESQL) {
			zc2.getZConnection().setAutoCommitTrue();
		}

		// FIXME 2025年2月4日 下午7:11:01 zhangzhen :  pg mysql sqlite 都试了，似乎这行重置也没必要，暂时注释掉
		//				resetToDefaultTransactionIsolationAndSetAutoCommitFalse(zc2);

		if ((isolationEnum != null) && (isolationEnum != ZIsolationEnum.DEFAULT)
				&& (zc2.getZConnection().getTransactionIsolation() != isolationEnum.getIsolation())) {
			zc2.getZConnection().setTransactionIsolation(isolationEnum);
			zc2.getZConnection().setTransactionIsolationChanged(true);
		}

		ZCONNECTION_THREADLOCAL.set(zc2);

		return defaultDatsourceName;
	}

	public static void resetToDefaultTransactionIsolation(final ZC2 zc2) {
		zc2.getZConnection().resetToDefaultTransactionIsolationIfChanged();
	}

	@Pointcut("@annotation(vo.zrepository.transaction.ZTransaction)")
	public void pointcut() {

	}

	@Override
	public Object before(final AOPParameter aopParameter) {
		return null;
	}

	/**
	 * vortex 的 AOP方法，拦截：@ZAOP(interceptType = ZTransaction.class)
	 * 本方法逻辑和springAOP public final Object around(final ProceedingJoinPoint proceedingJoinPoint)
	 * 方法逻辑一致，都是在[一个或一组操作DB的目标方法]执行前获取一个ZConnection，然后这一组操作都使用
	 * 这一个ZConnection，全部执行成功则commit，有一个执行异常则rollback.
	 *
	 */
	@Override
	public Object around(final AOPParameter aopParameter) {

		final Method method = aopParameter.getMethod();
		final String defaultDatsourceName = ZTransactionAOP.before(method);

		try {

			ZCONNECTION_THREADLOCAL.get().getZConnection().setAutoCommitFalse();

			final Object v = aopParameter.invoke();
			commit();
			return v;
		} catch (final Throwable e) {
			e.printStackTrace();
			rollback();

		} finally {

			resetToDefaultTransactionIsolation(ZCONNECTION_THREADLOCAL.get());
			ZCONNECTION_THREADLOCAL.get().getZConnection().setAutoCommitTrue();

			ZCPool.getInstance(defaultDatsourceName)
			.returnZConnectionAndCommit(ZCONNECTION_THREADLOCAL.get().getZConnection());

			ZRC.singleton().clear(ZCONNECTION_THREADLOCAL.get().getKeyList());

			clear();
		}

		return null;
	}

	@Override
	public Object after(final AOPParameter aopParameter) {
		return null;
	}
}
