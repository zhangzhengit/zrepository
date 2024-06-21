package com.vo;

import java.lang.reflect.Field;
import java.util.Date;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

/**
 * @ZCreateTime 字段 ZRepository.save 时的动作：如果字段值为null，则自动给一个初始值[当前时间]
 *
 * @author zhangzhen
 * @date 2024年6月16日 上午5:25:45
 *
 */
@ZOrder
public class ZCreateTimeHandler extends ZSaveHandler {

	public static final ImmutableSet<Class<?>> SUPPORTED_CLASS_SET = ImmutableSet.copyOf(Sets.newHashSet(Date.class));

	@Override
	public SUA handle(final SUA sua) {
		// FIXME 2024年6月16日 上午5:28:09 zhangzhen : 校验是什么类型和是否有@ZDateFormat注解，然后在此改赋值的类型
		final java.util.Date now = new Date();
		final Field[] fs = sua.getEntityClass().getDeclaredFields();
		for (final Field f : fs) {
			if (f.isAnnotationPresent(ZCreateTime.class)) {
				f.setAccessible(true);
				try {
					final Object zvv = f.get(sua.getEntityObject());
					if (zvv == null) {
						f.set(sua.getEntityObject(), now);
					}
				} catch (IllegalArgumentException | IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		}

		return sua;
	}
}
