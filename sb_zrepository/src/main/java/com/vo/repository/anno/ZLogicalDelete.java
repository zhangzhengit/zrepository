package com.vo.repository.anno;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用在 @see @ZEntity 标记的类里的字段上，如：
 *
 @ZEntity(tableName = "blobt")
 public class BlobEntity {

	@ZID
	private Integer id;

	private String name;

  	@ZLogicalDelete
	private Integer isDelete;
}

正常声明：
 	public interface BlobRepository extends ZRepository<BlobEntity, Integer>{}

正常使用：
	blobRepository.deleteById

  程序会自动判断	@ZEntity 类中是否存在 @ZLogicalDelete 字段：
 	有：
	  1、则执行[ZRepository.deleteById]操作时改用逻辑删除：update is_delete = @ZLogicalDelete.deleted() where id = ?
	  2、并且所有的[select]操作(手动定义的@ZQuery除外)都自动加入where条件：is_delete = @ZLogicalDelete.undeleted()
  	  3、[ZRepository.save] 时自动给 @ZLogicalDelete 字段赋值为 @ZLogicalDelete.undeleted()

  	无：则所有操作都是正常流程

 执行 [ZRepository.update] 时，带本注解的字段值是什么，就update为什么，不做特殊判断

 *
 * @author zhangzhen
 * @date 2024年6月16日 下午4:54:43
 *
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
public @interface ZLogicalDelete {

	// FIXME 2024年6月20日 上午7:57:48 zhangzhen : 考虑允许自定义类型和值，如String 类型Y/N 、BOOL类型F/T 等等
	public static final int DELETED = 1;
	public static final int NOT_DELETED = 0;

	/**
	 * 删除了的
	 *
	 * @return
	 */
	int deleted() default DELETED;

	/**
	 * 未删除的
	 *
	 * @return
	 */
	int undeleted() default NOT_DELETED;

}
