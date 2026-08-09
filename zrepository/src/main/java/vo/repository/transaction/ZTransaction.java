package vo.repository.transaction;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 事务注解，用在方法上，本方法内所有的 ZRepository 的操作都使用同一个Connection对象。
 * 方法执行成功则自动提交，方法执行出现异常(包括程序执行中未知的不可预料的异常和代码中手动抛出的异常)则回滚。
 * 如果方法流程没达到预期，但程序既不会抛出异常，用户也不想抛出异常，也可以使用 ZTransactionAOP.rollback() 进行回滚。
 *
 *
 * 注意：如果本注解标记的方法里面有不同数据源的 ZRepository 子接口
 * 	则可能会出现不可预料的情况，因为 ZTransactionAOP 类使用 ThreadLocal
 * 	来暂存java.sql.Connection，多个数据源的 ZRepository 来回执行会使ThreadLocal来回存储
 *  不同数据源的Connection，导致下一个操作取到的Connection是其他数据源的。
 *
 * @author zhangzhen
 * @date 2023年6月17日
 *
 */
// FIXME 2024年7月14日 下午11:05:25 zhangzhen : 上面本次添加的javadoc只是推测，还没测试
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
public @interface ZTransaction {

	/**
	 * 给本注解标记的方法单独指定一个隔离级别
	 *
	 * 默认值为 DEFAULT，即：数据库默认是什么隔离级别就使用什么隔离级别，使用数据库默认的隔离级别
	 *
	 * @return
	 */
	ZIsolationEnum isolation() default ZIsolationEnum.DEFAULT;

}
