package com.vo;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Optional;

/**
 *
 * @ZLogicalDelete 字段 ZRepository.save 时的动作：如果字段值为null，则自动给一个初始值[ZLogicalDelete.undeleted()]
 *
 * @author zhangzhen
 * @date 2024年6月19日 下午5:44:32
 *
 */
@ZOrder(value = 1)
public class ZLogicalDeleteSaveHandler extends ZSaveHandler {

	@Override
	public SUA handle(final SUA sua) {

		final Optional<Field> zldf = Arrays.stream(sua.getEntityClass().getDeclaredFields()).filter(f -> f.isAnnotationPresent(ZLogicalDelete.class)).findAny();
		if (!zldf.isPresent()) {
			return sua;
		}

		final Field f = zldf.get();
		f.setAccessible(true);
		final int undeleted = f.getAnnotation(ZLogicalDelete.class).undeleted();
		try {
			f.set(sua.getEntityObject(), undeleted);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}

		return sua;
	}

}
