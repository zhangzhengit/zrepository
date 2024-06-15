package com.vo;

import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.stream.Collectors;

import cn.hutool.core.util.ClassUtil;

/**
 *
 * 暂存扫描的结果
 *
 * @author zhangzhen
 * @date 2023年9月5日
 *
 */
public class ClassMap {

	private static final Map<String, Set<Class<?>>> MAP = new WeakHashMap<>();

	public synchronized static Set<Class<?>> scanPackage(final String packageName, final Class<?> cls) {
		final Set<Class<?>> cs = scanPackage(packageName);
		final Set<Class<?>> rs = cs.stream().filter(c -> c.equals(cls)).collect(Collectors.toSet());
		return rs;
	}

	public synchronized static Set<Class<?>> scanPackage(final String packageName) {
		final Set<Class<?>> v = MAP.get(packageName);
		if (v != null) {
			return v;
		}

		final Set<Class<?>> clsSet = ClassUtil.scanPackage(packageName);
		MAP.put(packageName, clsSet);

		return clsSet;
	}
}

