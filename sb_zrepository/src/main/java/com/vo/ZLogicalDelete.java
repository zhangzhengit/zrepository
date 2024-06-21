package com.vo;

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
 判断 @ZEntity 是否存在 @ZLogicalDelete 字段：
 	有：
	  1、则执行[ZRepository.deleteById]操作时使用逻辑删除：update is_delete = @ZLogicalDelete.deleted() where id = ?
	  2、并且[select]操作(手动定义的@ZQuery除外)都自动加入where条件：is_delete = @ZLogicalDelete.undeleted()
  	  3、[ZRepository.save] 时自动给is_delete字段赋值为 @ZLogicalDelete.undeleted()

  	无：
	  1、则[ZRepository.deleteById]为物理删除：delete where id = ?
	  2、并且[select]操作不会自动加入[is_delete = @ZLogicalDelete.undeleted()]的where条件

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
