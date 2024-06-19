package com.vo;

import java.lang.reflect.Field;
import java.util.Date;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

/**
 * @ZUpdateTime字段 ZRepository.update 时的动作：直接忽略字段值，赋值为[当前时间]
 *
 * @author zhangzhen
 * @date 2024年6月16日 上午6:10:30
 *
 */
public class ZUpdateTimeHandler extends ZUpdateHandler {

	public static final ImmutableSet<Class<?>> SUPPORTED_CLASS_SET = ImmutableSet.copyOf(Sets.newHashSet(Date.class));

	@Override
	public SUA handle(final SUA sua) {
		final java.util.Date now = new Date();
		final Field[] fs = sua.getEntityClass().getClass().getDeclaredFields();
		for (final Field f : fs) {
			if (f.isAnnotationPresent(ZUpdateTime.class)) {
				f.setAccessible(true);
				try {
					f.set(sua.getEntityObject(), now);
				} catch (IllegalArgumentException | IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		}

		return sua;
	}

}
