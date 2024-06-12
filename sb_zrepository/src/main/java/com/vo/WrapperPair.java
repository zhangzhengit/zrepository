package com.vo;

import lombok.Data;

/**
 * 构造查询条件的键值对
 * 如：
 * 	  需要构造查询条件为: WHERE name = ? and id = ?
 *
 * 构造代码：
 * 	final ZRWrapper<MyEntity> eq = new ZRWrapper<>();
	eq.eq(Lists.newArrayList(
			new WrapperPair<>(MyEntity::getName, "zhangsan"),
			new WrapperPair<>(MyEntity::getId, 200)));

	final List<MyEntity> find = this.myRepository.find(eq);

   则生成sql：
   	SELECT [MyEntity中的所有字段] FROM table名称 WHERE (name = 'zhangsan' AND id = 200);

  其他查询需求，根据 @see ZRWrapper 中提供的方法来组合即可
 *
 * @author zhangzhen
 * @date 2024年6月12日 下午7:47:08
 *
 */
@Data
public class WrapperPair<T> {

	/**
	 * 方法引用，如：MyEntity::getName()
	 */
	private final SerializableFunction<T, Object> function;

	/**
	 * 方法引用对应的值，如："zhangsan"
	 */
	private final Object value;

	public WrapperPair() {
		this.function = null;
		this.value = null;
	}

	public WrapperPair(final SerializableFunction<T, Object> function, final Object value) {
		this.function = function;
		this.value = value;
	}

	public WrapperPair(final SerializableFunction<T, Object> function) {
		this.function = function;
		this.value = null;
	}

	public WrapperPair(final Object value) {
		this.function = null;
		this.value = value;
	}

}
