package com.vo;

import java.lang.reflect.Field;
import java.util.Date;

/**
 * @ZVersion save 时的动作：如果带 @ZCreateTime 的字段值为null，则自动给一个初始值(当前世界)
 *
 * @author zhangzhen
 * @date 2024年6月16日 上午5:25:45
 *
 */
public class ZCreateTimeHandler extends ZSaveHandler {

	@Override
	public void handle(final Object entityObject) {
		// FIXME 2024年6月16日 上午5:28:09 zhangzhen : 校验是什么类型和是否有@ZDateFormat注解，然后在此改赋值的类型
		final java.util.Date now = new Date();
		final Field[] fs = entityObject.getClass().getDeclaredFields();
		for (final Field f : fs) {
			if (f.isAnnotationPresent(ZCreateTime.class)) {
				f.setAccessible(true);
				try {
					final Object zvv = f.get(entityObject);
					if (zvv == null) {
						f.set(entityObject, now);
					}
				} catch (IllegalArgumentException | IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
