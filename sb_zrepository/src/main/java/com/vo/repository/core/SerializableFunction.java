package com.vo.repository.core;

import java.io.Serializable;
import java.util.function.Function;

/**
 * 继承了Serializable 的Function
 * 
 * @param <T>
 * @param <R>
 *
 * @author zhangzhen
 * @date 2024年6月11日 下午10:29:53
 * 
 */
@FunctionalInterface
public interface SerializableFunction<T, R> extends Function<T, R>, Serializable {

}
