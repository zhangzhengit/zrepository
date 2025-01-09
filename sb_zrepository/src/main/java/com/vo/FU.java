package com.vo;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.WeakHashMap;

/**
 *
 *
 * @author zhangzhen
 * @date 2025年1月10日 上午12:13:10
 *
 */
public class FU {

	private final static Map<String, Map<String, Object>> TYPE_CACHE = new WeakHashMap<>(128, 1F);
	//	private final static Map<String, Object> F_CACHE = new WeakHashMap<>(128, 1F);

	public static Field getDeclaredField(final Class<?> type, final String javaFieldName)
			throws NoSuchFieldException, SecurityException {

		final String key = type.getName();
		final Map<String, Object> fM = TYPE_CACHE.get(key);
		if (fM != null) {
			final Object f = fM.get(javaFieldName);
			if (f != null) {
				return (Field) f;
			}
		}

		synchronized (key) {
			final Field f = getDeclaredField0(javaFieldName, type);
			final Map<String, Object> fMK = TYPE_CACHE.get(key);
			final Map<String, Object> fMN = fMK != null ? fMK : new WeakHashMap<>(16, 1F);
			fMN.put(javaFieldName, f);
			TYPE_CACHE.put(key, fMN);
			return f;
		}

	}

	private static Field getDeclaredField0(final String javaFieldName, final Class<?> type) throws NoSuchFieldException, SecurityException {
		return type.getDeclaredField(javaFieldName);
	}

}
