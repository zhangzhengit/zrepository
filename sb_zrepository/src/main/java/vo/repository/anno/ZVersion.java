package vo.repository.anno;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用在 @see @ZEntity 标记的类里的字段上，表示此字段为乐观锁控制字段.
 * 带有本注解的字段，在 ZRepository.save时会自动初始化字段值。
 * 全程无需手动维护字段值，就当这个字段不存在.
 *
 * 注意：需要在业务逻辑方法上加入　@see @ZTransaction 注解，里面进行update的
 * 		@ZEntity类里的 @ZVersion字段才会在上传SQL时自动加入：
 * 		UPDATE xxx, version = 旧值+1 WHERE version = 旧值 AND id = ?
 *
 *      方法不加入 @ZTransaction 注解的话，生成为：
 * 		UPDATE xxx WHERE id = ?
 *		即：不使用乐观锁.
 *
   用法：
	1、versionTestById 方法加入 @ZTransaction
	2、BlobEntity 存在一个 @ZVersion 字段
	3、修改数据后，使用ZRepository.update 进行更新
		执行语句： UPDATE xxx, version = 旧值+1 WHERE version = 旧值 AND id = ?
		如果update返回false，则说明没更新成功,WHERE之后的条件不满足
		需要抛异常让springAOP自动回滚或者手动回滚

   	@ZTransaction
	public void versionTestById(final Integer id) {

		final BlobEntity e = this.myRepository.findById(id);

		// 模拟修改数据
		e.setName("张三");
		e.setxxx
		e.setxxx

		// 进行update
		final boolean update = this.myRepository.update(e);
		if (!update) {
			// 在此需要进行回滚操作：

			// 方式1：抛异常，让springAOP类 自动回滚
			throw new NullPointerException("更新失败");

			// 方式2：直接使用springAOP类进行手动回滚
			ZTransactionAOP.rollback();
		}
	}


 * @author zhangzhen
 * @date 2024年6月15日 上午6:20:51
 *
 */
// FIXME 2024年6月16日 上午1:01:44 zhangzhen : 目前只支持了springAOP类，并且只做了很简单的测试:
// 写了一个update方法，然后并发执行这个方法。结果如预期。
// TODO : 支持zf,并且测试更复杂场景
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
public @interface ZVersion {

}
