package com.vo;

import java.lang.reflect.Field;

/**
 * @ZVersion save 时的动作：如果带 @ZVersion 的字段值为null，则自动给一个初始值
 *
 * @author zhangzhen
 * @date 2024年6月16日 上午4:48:06
 *
 */
public class ZVersionHandler extends ZSaveHandler {

	public static final long ZVERSION_INITIAL_VALUE = 0L;

	@Override
	public void handle(final Object entityObject) {
		final Field[] fs = entityObject.getClass().getDeclaredFields();
		for (final Field f : fs) {
			if (f.isAnnotationPresent(ZVersion.class)) {
				f.setAccessible(true);
				try {
					final Object zvv = f.get(entityObject);
					if (zvv == null) {
						f.set(entityObject, ZVERSION_INITIAL_VALUE);
					}
				} catch (IllegalArgumentException | IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public boolean condition(final Class<?> entityClass) {
		return false;
	}

}
