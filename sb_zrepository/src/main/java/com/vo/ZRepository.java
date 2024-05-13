package com.vo;

import java.util.List;

import com.vo.core.Page;


/**
 *
 * 顶级接口，自定义interface来 extends 此interface，注意自定义interface里面只可以有数据操作相关的方法.
 * 并且自定义interface仅 extends 此interface，不要实现任何其他接口
 *
 * 子接口中支持两种方法：

	FIXME 2024年5月13日 下午10:55:47 zhangzhen: 考虑怎么在启动时校验，有点复杂，因为支持很多种声明式方法，需要仔细分辨，并且以后还可能
 		支持更多形式，更要慎重。当前是否考虑先校验简单的比如 findByXX、findByXXAndXX、findByXXNot 之类的？

 * 1、声明式方法，参数类型、名称、顺序都必须和方法名称中保持一致，如：
 * 	List<BlobEntity> findByDateAndInteger1(Date date,Integer integer1);

 * 2、自定义查询，使用 @ZQuery 注解自定义SQL，如：
 * 	@ZQuery(sql = "select * from blobt where id >= ?")
	List<BlobEntity> selectGTEId(Integer id);

 *
 * @param <T>  @ZEntity 标记的类
 * @param <ID> @ZEntity 标记的类里的 @ZID 字段的类型
 *
 *
 */
// FIXME 2024年5月3日 下午8:41:30 zhangzhen: findByIsDelete 这种方法，isDelete为int类型，生成的sql 是 is_delete = null,
// 这种情况是否合理？是否根据column类型来确定用 is null 或者 = null?

// FIXME 2024年5月10日 下午10:21:52 zhangzhen: blob 类型 = 可以了，in 还是有问题

public interface ZRepository<T, ID> {
	// FIXME 2023年9月11日 下午8:35:46 zhanghen: TODO
	//	1、子接口中自定义方法的返回值，可以自定义返回类型中的T，做到隐藏敏感字段和去除非必要的select 字段
	//  2、select * 也改为 select 具体字段，自定义类型T使用getDeclaredFields来获取字段来生成具体的字段


	// FIXME 2023年9月6日 下午7:40:07 zhanghen: TODO 分页

	/**
	 * 分页查询，按T中非空字段等值查询，有多个非空字段则用and连接
	 *
	 * @param t    查询条件，根据对象里非null的字段来查询，等值查询
	 * @param page 第几页，从1开始
	 * @param size 一页显示几条
	 * @return
	 *
	 */
	// FIXME 2024年5月4日 下午10:46:33 zhangzhen: TODO :支持了blob类型后，page还要改，包括其他方法都要重新仔细测试
	Page<T> page(T t, Integer page, Integer size);

	/**
	 * select count(*) from 表
	 *
	 * @return
	 *
	 */
	Long count();

	/**
	 * 根据 @ZID 字段查询一个对象
	 *
	 * @param id
	 * @return
	 *
	 */
	T findById(ID id);

	/**
	 * 根据 @ZID 字段值来update一个对象，字段是什么就update为什么包括null， @ZID 字段不能为空，否则抛异常
	 *
	 * @param t
	 * @return 返回update后的对象
	 *
	 */
	T update(T t);

	/**
	 * insert一个对象，忽略 @ZID 字段，其他字段值什么是insert为什么，包括null
	 *
	 * @param t
	 * @return 返回新插入的对象
	 *
	 */
	T save(T t);

	/**
	 * 批量insert，忽略 @ZID 字段，其他字段值什么是insert为什么，包括null
	 *
	 * @param tList
	 * @return 返回插入对象的ID
	 *
	 */
	List<ID> saveAll(List<T> tList);

	/**
	 * 根据 @ZID 字段批量查询
	 *
	 * @param idList
	 * @return
	 *
	 */
	List<T> findByIdIn(List<ID> idList);

	/**
	 * 查询出所有的内容，不带条件
	 *
	 * @return
	 */
	List<T> findAll();

	/**
	 * 根据 @ZID 字段判断对象是否存在
	 *
	 * @param id
	 * @return
	 *
	 */
	boolean existById(ID id);

	/**
	 * 根据 @ZID 字段删除一个对象
	 *
	 * @param id
	 * @return 是否成功删除了
	 *
	 */
	boolean deleteById(ID id);

	/**
	 * 根据 @ZID 字段批量删除对象
	 *
	 * @param idList
	 * @return
	 *
	 */
	boolean deleteByIdIn(List<ID> idList);

	/**
	 * 删除全部的对象
	 *
	 * @return 是否全部删除了
	 *
	 */
	boolean deleteAll();

}
