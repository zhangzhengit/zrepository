package com.vo;

import java.util.List;
import java.util.Map;

import com.vo.core.Page;
import com.vo.core.Sort;


/**
 *
 * 顶级接口，自定义interface来 extends 此interface，注意自定义interface里面只可以有数据操作相关的方法.
 * 并且自定义interface仅 extends 此interface，不要实现任何其他接口
 *
 * 子接口中支持两种方法：

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
public interface ZRepository<T, ID> {

	/**
	 * 分页查询，按T中非空字段等值查询，有多个非空字段则用and连接
	 *
	 * @param t    查询条件，根据对象里非null的字段来查询，等值查询
	 * @param sort 排序条件
	 * @param page 第几页，从1开始
	 * @param size 一页显示几条
	 * @return
	 *
	 */
	// FIXME 2024年5月4日 下午10:46:33 zhangzhen: TODO :支持了blob类型后，page还要改，包括其他方法都要重新仔细测试
	Page<T> page(T t, Sort<T> sort, Integer page, Integer size);

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
	 * @return
	 *
	 */
	boolean update(T t);

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
	 * 根据一组 @ZID 字段判断对象是否存在
	 *
	 * @param idList
	 * @return 返回Map<@ZID字段值,是否存在Boolean值>
	 */
	Map<ID, Boolean> existByIdIn(List<ID> idList);

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
	// FIXME 2024年5月14日 下午8:09:51 zhangzhen: mysql deleteByIdIn 一千万个id 报错：怎么办，是提示修改server参数，还是限制此方法不能传太多参数？
	// Packet for query is too large (78,888,929 > 52,428,800). You can change this value on the server by setting the 'max_allowed_packet' variable.
	boolean deleteByIdIn(List<ID> idList);

	/**
	 * 删除全部的对象
	 *
	 * @return 是否全部删除了,表里没有数据会返回false,全部删除了会返回true
	 *
	 */
	boolean deleteAll();

	/**
	 * 手动构造条件来查询，动态查询，有参数值的方法都自动忽略掉传值null的where条件.
	 * 如：
			w.in(BlobEntity::getName, null);
			w.eq(BlobEntity::getId, 200);

		会直接忽略掉name条件：where id = 200

		需要判断某个字段为NULL/不为NULL，使用isNull/notNull方法.

	 * @param wrapper
	 * @return
	 */
	List<T> find(ZRWrapper<T> wrapper);

}
