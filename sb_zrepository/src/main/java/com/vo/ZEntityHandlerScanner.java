package com.vo;

import java.util.HashMap;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 *
 *
 * @author zhangzhen
 * @date 2024年6月16日 上午4:56:47
 *
 */
public class ZEntityHandlerScanner {

	public static void scan(final String packageName) {
		final Set<ZEntityHandler> saveHS = getZEntityHandlerSubClass(packageName, ZSaveHandler.class);
		set(ZEHEnum.SAVE, saveHS);

		final Set<ZEntityHandler> updateHS = getZEntityHandlerSubClass(packageName, ZUpdateHandler.class);
		set(ZEHEnum.UPDATE, updateHS);


		final Set<ZEntityHandler> DELETEDHS = getZEntityHandlerSubClass(packageName, ZDeleteHandler.class);
		set(ZEHEnum.DELETE_Logical, DELETEDHS);

		final Set<ZEntityHandler> EXCLUDED_DELETEDHS = getZEntityHandlerSubClass(packageName, ZAllHandler.class);
		set(ZEHEnum.SELECT_EXCLUDED_DELETED, EXCLUDED_DELETEDHS);
		// FIXME 2024年6月16日 上午5:07:55 zhangzhen : 继续写

	}

	private static Set<ZEntityHandler> getZEntityHandlerSubClass(final String packageName, final Class<?> acls) {
		final Set<ZEntityHandler> ssss = Sets.newHashSet();
		for (final Class<?> cls : ClassMap.scanPackage(packageName)) {
			final Class<?> ia = cls.getSuperclass();
			if (ia == null) {
				continue;
			}
			//			for (final Class<?> i : ia) {
			final boolean isZRSubclass = ia.equals(acls);
			if (isZRSubclass) {
				try {
					ssss.add((ZEntityHandler) cls.newInstance());
				} catch (InstantiationException | IllegalAccessException e) {
					e.printStackTrace();
				}
			}
			//			}
		}

		return ssss;
	}

	private static final HashMap<ZEHEnum, Set<ZEntityHandler>> m = Maps.newHashMap();

	private static void set(final ZEHEnum zehEnum,final Set<ZEntityHandler> saveHS ) {
		m.put(zehEnum, saveHS);
	}

	public static Set<ZEntityHandler> get(final ZEHEnum zehEnum) {
		return m.get(zehEnum);
	}

}
