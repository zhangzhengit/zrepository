package com.vo;

import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Supplier;

/**
 * 缓存类
 *
 * @author zhangzhen
 * @date 2024年6月29日 下午9:16:26
 *
 */
public class ZRC {

	private final static String PRIFEX = "cache:";
	private final static Map<String, Object> CACHE = new WeakHashMap<>();

	@SuppressWarnings("unchecked")
	public static <T> T computeIfAbsent(final Object key, final Supplier<T> supplier) {

		final String k = PRIFEX
				+ (key instanceof String ? key : key.getClass().getCanonicalName() + "@" + key.hashCode());

		final Object v = CACHE.get(k);
		if (v != null) {
			return (T) v;
		}

		synchronized (k.intern()) {
			final Object v2 = supplier.get();
			CACHE.put(k, v2);
			return (T) v2;
		}
	}

}
